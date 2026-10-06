import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';

const read = (path) => fs.readFileSync(new URL(path, import.meta.url), 'utf8');

test('public authentication exposes student signup but not faculty signup', () => {
  const app = read('../src/App.jsx');
  const staffLogin = read('../src/pages/staff/StaffLogin.jsx');
  const authService = read('../src/services/authService.js');
  assert.match(app, /path="\/signup"/);
  assert.doesNotMatch(app, /path="\/staff\/signup"/);
  assert.doesNotMatch(staffLogin, /Create Faculty Account|\/staff\/signup/);
  assert.doesNotMatch(authService, /signupFaculty|faculty\/signup/);
});
