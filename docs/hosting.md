# 🌐 Hosting Artifact-Shield Documentation

Artifact-Shield documentation is written in **Markdown**, making it easy to version control alongside the code. For a professional, hosted experience, we recommend the following options:

---

## 🐙 1. GitHub Pages (Recommended / Free)
GitHub Pages is the easiest way to host these docs directly from your repository.

### **Setup Steps**:
1.  Go to your GitHub Repository **Settings** > **Pages**.
2.  Enable GitHub Pages and set the source to the `main` branch and `/docs` folder (or root `/`).
3.  GitHub will provide a URL like `https://your-org.github.io/artifact-shield/`.
4.  Standard GitHub Markdown rendering will be used.

---

## ⚡ 2. Docsify (Live / Zero Build)
[Docsify](https://docsify.js.org/) generates your documentation website on the fly without any static build steps.

### **Setup Steps**:
1.  Initialize Docsify in the `docs` folder:
    ```bash
    npm i docsify-cli -g
    docsify init ./docs
    ```
2.  Commit the generated `index.html`.
3.  Deploy the `docs` folder to any static host (GitHub Pages, Vercel, Netlify).
4.  **Result**: Your `.md` files will be rendered as a beautiful, SPA (Single Page Application) with a sidebar and search.

---

## 🛠️ 3. MkDocs (Static Site Generator)
[MkDocs](https://www.mkdocs.org/) is a static site generator that is geared towards building project documentation.

### **Setup Steps**:
1.  Install MkDocs and the Material theme:
    ```bash
    pip install mkdocs mkdocs-material
    ```
2.  Create an `mkdocs.yml` in the root:
    ```yaml
    site_name: Artifact-Shield
    theme:
      name: material
    nav:
      - Home: README.md
      - Troubleshooting: docs/troubleshooting.md
      - Security: docs/security-integration.md
    ```
3.  Build and deploy:
    ```bash
    mkdocs gh-deploy
    ```

---

## 💎 4. VitePress (Modern / Tailwind Aesthetic)
[VitePress](https://vitepress.dev/) is the "modern Pandoc" for documentation. It is extremely fast and focuses on a **minimalist, premium, Tailwind-like UI**.

### **Setup Steps**:
1.  Initialize VitePress in your project root:
    ```bash
    npx vitepress init
    ```
2.  Choose "Default Theme" for a clean, modern look.
3.  VitePress will automatically link your `README.md` and `docs/*.md` files.

---

## 🦖 5. Docusaurus (Standard / Feature Rich)
[Docusaurus](https://docusaurus.io/) is the industry-standard tool for open-source documentation.

### **Setup Steps**:
1.  Initialize Docusaurus:
    ```bash
    npx create-docusaurus@latest website classic
    ```

---

## 🏛️ 6. Internal Hosting (Company Wiki)
If you need to keep your documentation internal:
1.  **Confluence**: Use the "Markdown Macro" to import your `.md` guides.
2.  **Microsoft SharePoint**: Render your docs directly via "Markdown" web parts.

---
*Developed by **Dhoondlay Engineering** for high-security enterprise environments.*
