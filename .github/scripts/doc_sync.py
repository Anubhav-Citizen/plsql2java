"""
Shared utilities for plsql2java doc↔GitHub Discussions sync.
"""
import os, re, json, requests
from pathlib import Path

TOKEN      = os.environ["GH_TOKEN"]
REPO       = os.environ["REPO"]                         # owner/repo
REPO_OWNER = REPO.split("/")[0]
REPO_NAME  = REPO.split("/")[1]

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

# ── Category definitions ──────────────────────────────────────────────────────
# Maps a Discussion category name to the aidlc-docs folder glob it covers.
CATEGORIES = [
    {
        "name":        "📋 Functional Requirements",
        "description": "Functional requirement documents (FR-xx)",
        "folder":      "aidlc-docs/inception/requirements",
        "emoji":       "📋",
        "doc_type":    "FR",
    },
    {
        "name":        "⚙️ Technical Requirements",
        "description": "Non-functional / technical requirement documents (NFR / TR)",
        "folder":      "aidlc-docs/construction",
        "subfolder":   "nfr-requirements",
        "emoji":       "⚙️",
        "doc_type":    "TR",
    },
    {
        "name":        "🏗️ Application Design",
        "description": "High-level application architecture and component design",
        "folder":      "aidlc-docs/inception/application-design",
        "emoji":       "🏗️",
        "doc_type":    "AD",
    },
    {
        "name":        "🔧 Functional Design",
        "description": "Per-unit functional design documents",
        "folder":      "aidlc-docs/construction",
        "subfolder":   "functional-design",
        "emoji":       "🔧",
        "doc_type":    "FD",
    },
    {
        "name":        "☁️ Infrastructure Design",
        "description": "Deployment and infrastructure design documents",
        "folder":      "aidlc-docs/construction",
        "subfolder":   "infrastructure-design",
        "emoji":       "☁️",
        "doc_type":    "ID",
    },
    {
        "name":        "📖 User Stories",
        "description": "User stories and personas",
        "folder":      "aidlc-docs/inception/user-stories",
        "emoji":       "📖",
        "doc_type":    "US",
    },
]

CATEGORY_BY_NAME = {c["name"]: c for c in CATEGORIES}
CATEGORY_BY_TYPE = {c["doc_type"]: c for c in CATEGORIES}


# ── Enable Discussions on the repo ───────────────────────────────────────────

def enable_discussions():
    r = requests.patch(
        f"{API}/repos/{REPO}",
        headers=REST_HEADERS,
        json={"has_discussions": True},
    )
    if r.status_code == 200:
        print("Discussions enabled on repository.")
    else:
        print(f"Could not enable discussions (may already be enabled): {r.status_code}")


# ── GraphQL helpers ───────────────────────────────────────────────────────────

def gql(query: str, variables: dict) -> dict:
    r = requests.post(GQL, headers=GQL_HEADERS,
                      json={"query": query, "variables": variables})
    return r.json()


def get_repo_id() -> str:
    data = gql("""
    query($owner: String!, $name: String!) {
      repository(owner: $owner, name: $name) { id }
    }""", {"owner": REPO_OWNER, "name": REPO_NAME})
    return data["data"]["repository"]["id"]


def get_discussion_categories(repo_id: str) -> dict[str, str]:
    """Returns {category_name: category_id}."""
    data = gql("""
    query($id: ID!) {
      node(id: $id) {
        ... on Repository {
          discussionCategories(first: 25) {
            nodes { id name }
          }
        }
      }
    }""", {"id": repo_id})
    nodes = data["data"]["node"]["discussionCategories"]["nodes"]
    return {n["name"]: n["id"] for n in nodes}


def create_discussion_category(repo_id: str, name: str, description: str, emoji: str) -> str:
    """Create a discussion category and return its ID."""
    data = gql("""
    mutation($repoId: ID!, $name: String!, $description: String!, $format: DiscussionCategoryFormat!) {
      createDiscussionCategory(input: {
        repositoryId: $repoId,
        name: $name,
        description: $description,
        format: $format
      }) {
        discussionCategory { id name }
      }
    }""", {
        "repoId": repo_id,
        "name": name,
        "description": description,
        "format": "OPEN_ENDED",
    })
    result = data.get("data", {}).get("createDiscussionCategory", {})
    if result:
        cat_id = result["discussionCategory"]["id"]
        print(f"  Created category: {name} ({cat_id})")
        return cat_id
    print(f"  Could not create category {name}: {data.get('errors')}")
    return None


def ensure_categories(repo_id: str) -> dict[str, str]:
    """Ensure all required categories exist. Returns {name: id}."""
    existing = get_discussion_categories(repo_id)
    for cat in CATEGORIES:
        if cat["name"] not in existing:
            cat_id = create_discussion_category(
                repo_id, cat["name"], cat["description"], cat["emoji"])
            if cat_id:
                existing[cat["name"]] = cat_id
    return existing


def get_discussions(repo_id: str, category_id: str) -> dict[str, dict]:
    """Returns {title: {id, body, number}} for all discussions in a category."""
    data = gql("""
    query($id: ID!, $catId: ID!) {
      node(id: $id) {
        ... on Repository {
          discussions(first: 100, categoryId: $catId) {
            nodes { id number title body }
          }
        }
      }
    }""", {"id": repo_id, "catId": category_id})
    nodes = data["data"]["node"]["discussions"]["nodes"]
    return {n["title"]: n for n in nodes}


def create_discussion(repo_id: str, category_id: str, title: str, body: str) -> dict:
    data = gql("""
    mutation($repoId: ID!, $categoryId: ID!, $title: String!, $body: String!) {
      createDiscussion(input: {
        repositoryId: $repoId,
        categoryId: $categoryId,
        title: $title,
        body: $body
      }) {
        discussion { id number title url }
      }
    }""", {
        "repoId": repo_id,
        "categoryId": category_id,
        "title": title,
        "body": body,
    })
    return data.get("data", {}).get("createDiscussion", {}).get("discussion", {})


def update_discussion(discussion_id: str, title: str, body: str):
    gql("""
    mutation($id: ID!, $title: String!, $body: String!) {
      updateDiscussion(input: { discussionId: $id, title: $title, body: $body }) {
        discussion { id }
      }
    }""", {"id": discussion_id, "title": title, "body": body})


# ── File → category resolution ────────────────────────────────────────────────

def category_for_file(filepath: str) -> dict | None:
    """Return the CATEGORIES entry that matches a given aidlc-docs file path."""
    fp = filepath.replace("\\", "/")
    for cat in CATEGORIES:
        folder = cat["folder"].replace("\\", "/")
        if "subfolder" in cat:
            if f"/{cat['subfolder']}/" in fp and fp.startswith(folder):
                return cat
        else:
            if fp.startswith(folder):
                return cat
    return None


def collect_docs() -> list[dict]:
    """Walk aidlc-docs and return list of {path, title, body, category}."""
    docs = []
    skip = {"aidlc-state.md", "audit.md"}
    for root, dirs, files in os.walk("aidlc-docs"):
        # Skip plans/ — internal workflow artefacts, not design docs
        dirs[:] = [d for d in dirs if d != "plans"]
        for fname in files:
            if not fname.endswith(".md") or fname in skip:
                continue
            fpath = os.path.join(root, fname).replace("\\", "/")
            cat = category_for_file(fpath)
            if cat is None:
                continue
            with open(fpath) as f:
                content = f.read().strip()
            if not content:
                continue
            # Use first H1 as title, fallback to filename
            h1 = re.search(r'^# (.+)$', content, re.MULTILINE)
            title = h1.group(1).strip() if h1 else fname.replace(".md", "").replace("-", " ").title()
            docs.append({
                "path":     fpath,
                "title":    title,
                "body":     content,
                "category": cat,
            })
    return docs


# ── Structured doc generation from Discussion ─────────────────────────────────

_SECTION_TEMPLATES = {
    "FR": """\
# {title}

**Document Type**: Functional Requirements
**Source**: GitHub Discussion #{number}
**Category**: {category}

---

## Overview

{overview}

## Functional Requirements

{requirements}

## Acceptance Criteria

{acceptance}

## Out of Scope

{out_of_scope}

---

*Auto-generated from GitHub Discussion by sync-discussion-to-docs workflow.*
""",
    "TR": """\
# {title}

**Document Type**: Technical / Non-Functional Requirements
**Source**: GitHub Discussion #{number}
**Category**: {category}

---

## Overview

{overview}

## Non-Functional Requirements

{requirements}

## Performance Targets

{performance}

## Security Considerations

{security}

---

*Auto-generated from GitHub Discussion by sync-discussion-to-docs workflow.*
""",
    "AD": """\
# {title}

**Document Type**: Application Design
**Source**: GitHub Discussion #{number}
**Category**: {category}

---

## Architecture Overview

{overview}

## Components

{components}

## Design Decisions

{decisions}

## Data Flow

{data_flow}

---

*Auto-generated from GitHub Discussion by sync-discussion-to-docs workflow.*
""",
    "FD": """\
# {title}

**Document Type**: Functional Design
**Source**: GitHub Discussion #{number}
**Category**: {category}

---

## Overview

{overview}

## Business Logic

{business_logic}

## Domain Entities

{entities}

## Business Rules

{rules}

---

*Auto-generated from GitHub Discussion by sync-discussion-to-docs workflow.*
""",
    "ID": """\
# {title}

**Document Type**: Infrastructure Design
**Source**: GitHub Discussion #{number}
**Category**: {category}

---

## Overview

{overview}

## Deployment Architecture

{deployment}

## Infrastructure Components

{components}

## Configuration

{config}

---

*Auto-generated from GitHub Discussion by sync-discussion-to-docs workflow.*
""",
    "US": """\
# {title}

**Document Type**: User Story
**Source**: GitHub Discussion #{number}
**Category**: {category}

---

{overview}

---

*Auto-generated from GitHub Discussion by sync-discussion-to-docs workflow.*
""",
}

_SECTION_KEYS = {
    "FR": ["overview", "requirements", "acceptance", "out_of_scope"],
    "TR": ["overview", "requirements", "performance", "security"],
    "AD": ["overview", "components", "decisions", "data_flow"],
    "FD": ["overview", "business_logic", "entities", "rules"],
    "ID": ["overview", "deployment", "components", "config"],
    "US": ["overview"],
}


def _extract_sections(body: str, keys: list[str]) -> dict[str, str]:
    """
    Try to extract named sections from a discussion body.
    Falls back to distributing paragraphs across sections if no headers found.
    """
    result = {k: "" for k in keys}

    # Try to match ## Section headers in the body
    header_pat = re.compile(r'^##\s+(.+)$', re.MULTILINE)
    headers = [(m.start(), m.group(1).strip()) for m in header_pat.finditer(body)]

    if headers:
        for i, (pos, header) in enumerate(headers):
            end = headers[i + 1][0] if i + 1 < len(headers) else len(body)
            section_body = body[pos:end].split("\n", 1)[1].strip() if "\n" in body[pos:end] else ""
            # Match header to closest key
            header_lower = header.lower().replace(" ", "_")
            for key in keys:
                if key in header_lower or header_lower in key:
                    result[key] = section_body
                    break
            else:
                # Put in first empty key
                for key in keys:
                    if not result[key]:
                        result[key] = section_body
                        break
    else:
        # No headers — split paragraphs across keys
        paragraphs = [p.strip() for p in re.split(r'\n{2,}', body) if p.strip()]
        for i, key in enumerate(keys):
            result[key] = paragraphs[i] if i < len(paragraphs) else "_Not provided._"

    # Fill any empty sections
    for key in keys:
        if not result[key]:
            result[key] = "_Not provided._"

    return result


def discussion_to_doc(discussion: dict, cat: dict) -> str:
    """Generate a structured aidlc-docs markdown from a GitHub Discussion."""
    doc_type = cat["doc_type"]
    template = _SECTION_TEMPLATES.get(doc_type, _SECTION_TEMPLATES["FR"])
    keys     = _SECTION_KEYS.get(doc_type, ["overview"])
    sections = _extract_sections(discussion.get("body", ""), keys)
    return template.format(
        title    = discussion["title"],
        number   = discussion.get("number", "?"),
        category = cat["name"],
        **sections,
    )


def doc_path_for_discussion(discussion: dict, cat: dict) -> str:
    """Return the local file path where a discussion's doc should be written."""
    safe = re.sub(r'[^\w\-]', '-', discussion["title"].lower())[:60].strip('-')
    number = discussion.get("number", 0)
    folder = cat["folder"]
    return f"{folder}/discussion-{number}-{safe}.md"
