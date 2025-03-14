import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { provideMockStore } from '@ngrx/store/testing';
import { MenuComponent } from '../app/menu/menu.component';


const meta: Meta<MenuComponent> = {
  component: MenuComponent,
}

export default meta;
type Story = StoryObj<MenuComponent>;

export const MenuStory : Story = {
  decorators: [
    moduleMetadata({
      providers: [
        provideMockStore()
      ]
    })
  ]  
}
export const OnBoardingMenuStory : Story = {
  decorators: [
    moduleMetadata({
      providers: [
        provideMockStore({
          initialState: {
            "user": {
              user: {
                resource_access: {
                  members: {
                    roles: ["onboarding"]
                  }
                }
              }
            }
          }
        })
      ]
    })
  ]  
}

export const BoardMemberMenuStory : Story = {
  decorators: [
    moduleMetadata({
      providers: [
        provideMockStore({
          initialState: {
            "user": {
              user: {
                resource_access: {
                  members: {
                    roles: ["board-member"]
                  }
                }
              }
            }
          }
        })
      ]
    })
  ]
}
