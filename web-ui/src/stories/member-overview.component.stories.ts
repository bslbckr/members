import { applicationConfig, Meta, StoryObj } from "@storybook/angular";
import { MemberOverviewComponent } from '../app/member-overview/member-overview.component';
import { provideHttpClient } from "@angular/common/http";
import { http, HttpResponse } from 'msw';


const meta: Meta<MemberOverviewComponent> = {
  component: MemberOverviewComponent,
  decorators: [
    applicationConfig({
      providers: [provideHttpClient()]
    })
  ]
}

export default meta;

type Story = StoryObj<MemberOverviewComponent>;

const memberData = [
  {
    givenName: "Sylvester",
    name: "Fridaye",
    state: "ermäßigt",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  },{
    givenName: "Siffre",
    name: "Leadstone",
    state: "ermäßigt",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  },{
    givenName: "Shepperd",
    name: "Fitchet",
    state: "ermäßigt",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  }, {
    givenName: "Dino",
    name: "Nanni",
    state: "ermäßigt",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  },{
    givenName: "Britteny",
    name: "Serrell",
    state: "ermäßigt",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  },{
    givenName: "Kip",
    name: "Blackster",
    state: "jugendliche",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  }, {
    givenName: "Arlene",
    name: "Felmingham",
    state: "berufstätig",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05",
    exitDate: "2025-09-30"
  },{
    givenName: "Michael",
    name: "Liverseege",
    state: "passiv",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  }, {
    givenName: "Nicola",
    name: "Pratchett",
    state: "jugendliche",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  }, {
    givenName: "Vin",
    name: "Landell",
    state: "berufstätig",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  }, {
    givenName: "Claudine",
    name: "Grimm",
    state: "ermäßigt",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  }, {
    givenName: "Phillis",
    name: "Ballantyne",
    state: "jugendliche",
    entryDate: "2024-04-05",
    stateEffective: "2024-04-05"
  }
];

export const MemberOverviewStory : Story = {
  parameters: {
    msw: {
      handlers: [
        http.get('/api/v1/member', async () => {
          return HttpResponse.json(memberData)
        })
      ]
    }
  }
}
