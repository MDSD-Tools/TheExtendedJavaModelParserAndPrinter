// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const { themes } = require('prism-react-renderer');
const lightCodeTheme = themes.github;
const darkCodeTheme = themes.dracula;

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'The Extended Java Model Parser and Printer (JaMoPP)',
  tagline: 'Model Java Code in the Eclipse Modeling Framework (EMF)',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://mdsd-tools.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/TheExtendedJavaModelParserAndPrinter/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'MDSD-Tools', // Usually your GitHub org/user name.
  projectName: 'TheExtendedJavaModelParserAndPrinter', // Usually your repo name.

  onBrokenLinks: 'throw',

  markdown: {
    mermaid: true,
    hooks: {
      onBrokenMarkdownLinks: 'warn'
    }
  },
  trailingSlash: false,
  themes: ['@docusaurus/theme-mermaid'],

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          // editUrl:
          //   '',
        },
        blog: false,
        // blog: {
          // showReadingTime: true,
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          // editUrl:
          //   '',
        // },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      // image: 'img/docusaurus-social-card.jpg',
      navbar: {
        title: 'The Extended JaMoPP',
        logo: {
          alt: 'ExJaMoPP Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'docSidebar',
            position: 'left',
            label: 'Documentation',
          },
          {
            href: 'https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'More',
            items: [
              {
                label: 'Modelling for Continuous Software Engineering (MCSE) group',
                href: 'https://mcse.kastel.kit.edu/',
              },
              {
                label: 'GitHub Repository',
                href: 'https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter',
              },
            ],
          },
        ],
        copyright: `Copyright © 2023-2026 Modelling for Continuous Software Engineering (MCSE) group, Institute of Information Security and Dependability (KASTEL), Karlsruhe Institute of Technology (KIT).`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
        additionalLanguages: ['java']
      },
    }),
};

module.exports = config;
