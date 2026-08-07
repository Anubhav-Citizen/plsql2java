"""
Shared utilities for plsql2java story sync workflows.
"""
import os, re, json, time, requests

TOKEN       = os.environ["GH_TOKEN"]
REPO        = os.environ["REPO"]                        # owner/repo
PROJ_OWNER  = os.environ.get("PROJECT_OWNER", REPO.split("/")[0])
PROJ_NUM    = int(os.environ.get("PROJECT_NUMBER", "1"))

REST_HEADERS = {
    "Authorization": f"Bearer {TOKEN}",
    "Accept": "application/vnd.github+json",
    "X-GitHub-Api-Version": "2022-11-28",
}
GQL_HEADERS = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json",
}
API = "https://api.github.com"
GQL = "https://api.github.com/graphql"

EPIC_COLORS = ["0075ca","e4e669","d93f0b","0e8a16","1d76db","5319e7","f9d0c4","c2e0c6"]

STRIP_PREFIXES = ["PKG_", "TRG_", "SEQ_", "SP_", "FN_", "VW_"]


# ── Parsing ───────────────────────────────────────────────────────────────────

def parse_stories(md_path: str) -> list[dict]:
    """Parse stories.md → list of {title, body, epic, epic_number, story_number}."""
    with open(md_path) as f:
        content = f.read()

    epic_pat  = re.compile(r'^## (Epic (\d+): .+)$', re.MULTILINE)
    story_pat = re.compile(
        r'^### (Story (\d+)\.(\d+) — .+?)\n(.*?)(?=^### Story|\Z)',
        re.MULTILINE | re.DOTALL)

    epic_positions = [(m.start(), m.group(1), int(m.group(2))) for m in epic_pat.finditer(content)]

    def epic_for_pos(pos):
        name, num = "General", 0
        for ep_pos, ep_name, ep_num in epic_positions:
            if ep_pos <= pos:
                name, num = ep_name, ep_num
        return name, num

    stories = []
    for m in story_pat.finditer(content):
        epic_name, epic_num = epic_for_pos(m.start())
        stories.append({
            "title":        m.group(1).strip(),
            "body":         m.group(4).strip(),
            "epic":         epic_name,
            "epic_number":  epic_num,
            "story_number": f"{m.group(2)}.{m.group(3)}",
        })
    return stories


# ── GitHub REST helpers ───────────────────────────────────────────────────────

def get_existing_issues() -> dict[str, dict]:
    """Return map of issue title → issue dict for all open+closed issues."""
    issues = {}
    page = 1
    while True:
        r = requests.get(
            f"{API}/repos/{REPO}/issues",
            headers=REST_HEADERS,
            params={"state": "all", "per_page": 100, "page": page},
        )
        batch = r.json()
        if not batch:
            break
        for issue in batch:
            if "pull_request" not in issue:
                issues[issue["title"]] = issue
        page += 1
    return issues


def ensure_label(name: str, color: str, description: str = ""):
    existing = {l["name"] for l in
        requests.get(f"{API}/repos/{REPO}/labels?per_page=100",
                     headers=REST_HEADERS).json()
        if isinstance(l, dict)}
    if name not in existing:
        requests.post(f"{API}/repos/{REPO}/labels", headers=REST_HEADERS,
            json={"name": name, "color": color, "description": description})


def create_issue(title: str, body: str, labels: list[str]) -> dict:
    r = requests.post(f"{API}/repos/{REPO}/issues", headers=REST_HEADERS,
        json={"title": title, "body": body, "labels": labels})
    return r.json()


def update_issue_body(issue_number: int, body: str):
    requests.patch(f"{API}/repos/{REPO}/issues/{issue_number}",
        headers=REST_HEADERS, json={"body": body})


# ── GitHub Projects v2 GraphQL helpers ───────────────────────────────────────

def get_project_id() -> str:
    query = """
    query($login: String!, $number: Int!) {
      user(login: $login) { projectV2(number: $number) { id } }
    }"""
    r = requests.post(GQL, headers=GQL_HEADERS,
        json={"query": query, "variables": {"login": PROJ_OWNER, "number": PROJ_NUM}})
    return r.json()["data"]["user"]["projectV2"]["id"]


def add_issue_to_project(project_id: str, issue_node_id: str):
    mutation = """
    mutation($projectId: ID!, $contentId: ID!) {
      addProjectV2ItemById(input: {projectId: $projectId, contentId: $contentId}) {
        item { id }
      }
    }"""
    requests.post(GQL, headers=GQL_HEADERS,
        json={"query": mutation,
              "variables": {"projectId": project_id, "contentId": issue_node_id}})


# ── aidlc-docs document generation ───────────────────────────────────────────

def story_to_doc(story: dict) -> str:
    """Generate a structured aidlc-docs user story document from a GitHub issue story dict."""
    return f"""# {story['title']}

**Epic**: {story['epic']}
**Story Number**: {story['story_number']}
**Source**: GitHub Issue

---

{story['body']}

---

*Auto-generated from GitHub Issue by sync-issue-to-docs workflow.*
"""


def issue_to_story(issue: dict) -> dict:
    """Convert a raw GitHub issue into a story dict."""
    body = issue.get("body") or ""
    # Extract epic from body line "**Epic**: ..."
    epic_match = re.search(r'\*\*Epic\*\*:\s*(.+)', body)
    epic = epic_match.group(1).strip() if epic_match else "General"
    # Extract story number from title "Story X.Y — ..."
    num_match = re.search(r'Story (\d+\.\d+)', issue.get("title", ""))
    story_number = num_match.group(1) if num_match else "0.0"
    return {
        "title":        issue["title"],
        "body":         body,
        "epic":         epic,
        "epic_number":  int(story_number.split(".")[0]),
        "story_number": story_number,
    }


def doc_path_for_story(story: dict) -> str:
    """Return the relative aidlc-docs path for a story document."""
    safe_title = re.sub(r'[^\w\-]', '-', story['title'].lower())[:60].strip('-')
    return f"aidlc-docs/inception/user-stories/story-{story['story_number']}-{safe_title}.md"
