import { Preview } from '@storybook/angular';
import { initialize, mswLoader } from 'msw-storybook-addon';

initialize();

const preview: Preview = {
  loaders: [mswLoader]

};

export default preview;
