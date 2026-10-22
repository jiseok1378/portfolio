import { expect, test } from 'vitest';
import SiteFooter from './SiteFooter.vue';
import { mountQ } from '@/test-utils';
import type { Profile } from '@/content';

const blank: Profile = {
  name: '', title: 't', headline: '', email: '', phone: '', github: '', careerStart: '2020-06',
};

test('renders no contact links when email/phone/github are blank', () => {
  const w = mountQ(SiteFooter, { props: { profile: blank } });
  expect(w.findAll('a[href^="mailto:"]')).toHaveLength(0);
  expect(w.findAll('a[href^="tel:"]')).toHaveLength(0);
  expect(w.findAll('a[href*="github.com"]')).toHaveLength(0);
});

test('renders contact links when filled', () => {
  const w = mountQ(SiteFooter, {
    props: { profile: { ...blank, email: 'a@b.c', phone: '010-0000-0000', github: 'https://github.com/x' } },
  });
  expect(w.find('a[href="mailto:a@b.c"]').exists()).toBe(true);
  expect(w.find('a[href="tel:010-0000-0000"]').exists()).toBe(true);
  expect(w.find('a[href="https://github.com/x"]').exists()).toBe(true);
});
