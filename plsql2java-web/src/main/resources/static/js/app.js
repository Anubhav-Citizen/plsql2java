/* plsql2java home page — form submission logic */
document.addEventListener('DOMContentLoaded', () => {
    const uploadForm = document.getElementById('uploadForm');
    const jdbcForm = document.getElementById('jdbcForm');

    if (uploadForm) {
        uploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fileInput = document.getElementById('ddlFile');
            const mode = document.getElementById('modeSelect').value;
            const targetPackage = document.getElementById('targetPackage').value;

            if (!fileInput.files[0]) return;
            if (!fileInput.files[0].name.endsWith('.sql')) {
                fileInput.classList.add('is-invalid');
                return;
            }

            const formData = new FormData();
            formData.append('file', fileInput.files[0]);

            const uploadResp = await fetch('/api/migrations/upload', { method: 'POST', body: formData });
            if (!uploadResp.ok) { alert('Upload failed'); return; }
            const { uploadId } = await uploadResp.json();

            let jobId;
            if (mode === 'analyze') {
                const resp = await fetch('/api/migrations/analyze', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ uploadId })
                });
                ({ jobId } = await resp.json());
            } else {
                const resp = await fetch('/api/migrations/generate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ uploadId, targetPackage })
                });
                ({ jobId } = await resp.json());
            }
            window.location.href = '/progress/' + jobId;
        });
    }

    if (jdbcForm) {
        jdbcForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const body = {
                jdbcUrl: document.getElementById('jdbcUrl').value,
                username: document.getElementById('jdbcUser').value,
                password: document.getElementById('jdbcPassword').value,
                targetPackage: document.getElementById('jdbcPackage').value,
                confidenceThreshold: parseFloat(document.getElementById('confidenceThreshold').value)
            };
            const configResp = await fetch('/api/migrations/jdbc-config', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (!configResp.ok) { alert('Configuration failed'); return; }
            const { configId } = await configResp.json();

            const genResp = await fetch('/api/migrations/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ configId, targetPackage: body.targetPackage,
                                       confidenceThreshold: body.confidenceThreshold })
            });
            const { jobId } = await genResp.json();
            window.location.href = '/progress/' + jobId;
        });
    }
});
