# Frontend Components — Unit 6: Web Application Delivery

## Technology: Thymeleaf + Bootstrap 5 + Vanilla JS (SSE)

Thymeleaf server-side rendering with minimal JavaScript for SSE progress streaming and dynamic UI updates. No SPA framework required.

---

## Page: Home (`/`)

**Template**: `templates/index.html`

**Components**:
- Upload Form (`data-testid="ddl-upload-form"`)
  - File input: `data-testid="ddl-file-input"` (accept=".sql")
  - Submit button: `data-testid="upload-submit-button"`
- JDBC Config Form (`data-testid="jdbc-config-form"`)
  - JDBC URL input: `data-testid="jdbc-url-input"`
  - Username input: `data-testid="jdbc-user-input"`
  - Password input: `data-testid="jdbc-password-input"` (type=password)
  - Target package input: `data-testid="target-package-input"`
  - Confidence threshold input: `data-testid="confidence-threshold-input"`
  - Submit button: `data-testid="jdbc-config-submit-button"`
- Mode selector: Analyze / Generate (`data-testid="mode-selector"`)

**State**: Form submission → redirect to `/progress/{jobId}`

---

## Page: Progress (`/progress/{jobId}`)

**Template**: `templates/progress.html`

**Components**:
- Job ID display: `data-testid="job-id-display"`
- Status badge: `data-testid="job-status-badge"`
- Progress bar: `data-testid="progress-bar"` (Bootstrap progress, updated via SSE)
- Stage label: `data-testid="current-stage-label"`
- Log output area: `data-testid="progress-log"` (append-only textarea)
- Download button (hidden until complete): `data-testid="download-button"`
- Report button (hidden until complete): `data-testid="view-report-button"`

**SSE Logic** (inline JS):
```javascript
const evtSource = new EventSource('/api/migrations/{jobId}/events');
evtSource.onmessage = (e) => { /* update progress bar + log */ };
evtSource.addEventListener('complete', () => { /* show download/report buttons */ });
evtSource.addEventListener('error', () => { /* show error message */ });
```

---

## Page: Report View (`/report/{jobId}`)

**Template**: `templates/report.html`

**Components**:
- Report title: `data-testid="report-title"`
- Embedded HTML report: `data-testid="report-content"` (rendered in `<div>` via Thymeleaf `th:utext`)
- Download MD button: `data-testid="download-md-button"`
- Download HTML button: `data-testid="download-html-button"`
- Back to home: `data-testid="back-home-link"`

---

## Shared Layout

**Template**: `templates/layout.html` (Thymeleaf fragment)
- Navigation bar with app name and login/logout link
- Bootstrap 5 CSS (loaded from classpath static, not CDN — no SRI needed)
- All pages extend this layout

---

## Form Validation (Client-Side)

- Target package: regex `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$` validated on blur
- Confidence threshold: numeric, 0.0–1.0 range validated on blur
- File input: `.sql` extension check before submit
- All validation errors displayed inline below the field

---

## Accessibility

- All form inputs have associated `<label>` elements
- Error messages use `role="alert"`
- Progress bar uses `aria-valuenow`, `aria-valuemin`, `aria-valuemax`
- Buttons have descriptive text (no icon-only buttons)
