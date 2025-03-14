import { StorybookConfig } from '@storybook/angular';

const config: StorybookConfig = {
  addons: ['storybook-addon-angular-router'],
  stories: ['../src/stories/*.stories.ts'],
  framework: '@storybook/angular',
  staticDirs: ['../src']
};

export default config;
