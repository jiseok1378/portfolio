import { mount, type ComponentMountingOptions } from '@vue/test-utils';
import { Quasar } from 'quasar';
import type { Component } from 'vue';

/** Quasar 컴포넌트를 쓰는 화면을 테스트에서 띄운다. RouterLink 는 a 태그로 바꾼다. */
export function mountQ<T extends Component>(component: T, options: ComponentMountingOptions<T> = {}) {
  const global = options.global ?? {};
  return mount(component, {
    ...options,
    global: {
      ...global,
      plugins: [Quasar, ...(global.plugins ?? [])],
      stubs: {
        RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
        ...(global.stubs as Record<string, unknown> | undefined),
      },
    },
  } as ComponentMountingOptions<T>);
}
