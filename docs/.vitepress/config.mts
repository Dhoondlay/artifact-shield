import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Documentation",
  description: "acts as a \"Data Firewall.\" It sits between a user's prompt and the LLM. It uses deterministic algorithms (Regex, Luhn's Algorithm, and Entropy checks) to scrub sensitive data.",
  base: '/artifact-shield/',
  ignoreDeadLinks: true,
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Configuration', link: '/configuration-reference' }
    ],

    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Installation', link: '/installation' },
          { text: 'Deployment', link: '/deployment' },
          { text: 'How The Engine Works', link: '/how-it-works' },
          { text: 'API Reference', link: '/api-reference' },
          { text: 'Client Integration', link: '/client-integration' },
          { text: 'Configuration Reference', link: '/configuration-reference' },
          { text: 'Hosting Guide', link: '/hosting.md' }
        ]
      },
      {
        text: 'Enterprise Setup',
        items: [
          { text: 'Security (OIDC)', link: '/security-integration' },
          { text: 'Database Migration', link: '/database-migration' },
          { text: 'Observability', link: '/observability' }
        ]
      },
      {
        text: 'Resources',
        items: [
          { text: 'Troubleshooting', link: '/troubleshooting' },
          { text: 'Developer Guide', link: '/developer-guide' },
          { text: 'Security Scenarios', link: '/scenarios' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/dhoondlay/artifact-shield' }
    ]
  }
})
