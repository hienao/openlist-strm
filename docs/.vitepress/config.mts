import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  lang: 'zh_CN',
  title: "OpenList-Strm",
  description: "便捷的为你的OpenList影音文件生成Strm文件",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: '首页', link: '/' },
      { text: '部署',items: [
          { text: 'Docker部署', link: '/docker-deploy' }
        ] 
      },
      { text: '功能介绍',items: [
          { text: '快速开始', link: '/quick-start' },
          { text: '添加OpenList', link: '/add-openlist' },
          { text: '添加任务', link: '/add-task' },
          { text: '系统设置', link: '/system-config' }
        ] },
      { text: '更新历史', link: '/update-log' },
      { text: '参与开发', link: '/dev' },
      { text: '常见问题', link: '/faq' },
      { text: 'Examples', link: '/markdown-examples' }
    ],

    sidebar: [
      { text: '首页', link: '/' },
      { text: '部署',items: [
          { text: 'Docker部署', link: '/docker-deploy' }
        ] 
      },
      { text: '功能介绍',items: [
          { text: '快速开始', link: '/quick-start' },
          { text: '添加OpenList', link: '/add-openlist' },
          { text: '添加任务', link: '/add-task' },
          { text: '系统设置', link: '/system-config' }
        ] },
      { text: '更新历史', link: '/update-log' },
      { text: '参与开发', link: '/dev' },
      { text: '常见问题', link: '/faq' },
      {
        text: 'Examples',
        items: [
          { text: 'Markdown Examples', link: '/markdown-examples' },
          { text: 'Runtime API Examples', link: '/api-examples' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/hienao/openlist-strm' }
    ]
  }
})
