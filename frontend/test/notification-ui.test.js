import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const navbar = readFileSync(new URL('../src/components/Navbar.jsx', import.meta.url), 'utf8');
const styles = readFileSync(new URL('../src/styles.css', import.meta.url), 'utf8');

test('notification bell renders and toggles the dropdown', () => {
  assert.match(navbar, /aria-label="Notifications"/);
  assert.match(navbar, /setIsNotificationOpen\(\(open\) => !open\)/);
});

test('notification dropdown is out of layout flow and internally scrollable', () => {
  assert.match(styles, /\.notification-shell\s*\{[^}]*position:\s*relative/);
  assert.match(styles, /\.notification-panel\s*\{[^}]*position:\s*absolute/);
  assert.match(styles, /\.notification-panel\s*\{[^}]*max-height:\s*500px/);
  assert.match(styles, /\.notification-list\s*\{[^}]*overflow-y:\s*auto/);
});

test('unread count is attached to the bell and mark-read actions remain wired', () => {
  assert.match(navbar, /className="notification-badge"/);
  assert.match(navbar, /markNotificationRead\(notification\.notificationId\)/);
  assert.match(navbar, /setUnreadCount\(\(value\) => Math\.max\(0, value - 1\)\)/);
});

test('mark all, empty, loading, and error states are rendered inside the panel', () => {
  assert.match(navbar, /markAllNotificationsRead\(\)/);
  assert.match(navbar, /No notifications yet\./);
  assert.match(navbar, /Loading notifications/);
  assert.match(navbar, /Unable to load notifications\./);
  assert.match(navbar, /Retry/);
});

test('notification actions use safe internal navigation and close the panel', () => {
  assert.match(navbar, /resolveNotificationPath/);
  assert.match(navbar, /if \(!path\) return;\s*setIsNotificationOpen\(false\);/s);
  assert.match(navbar, /navigate\(path\);/);
  assert.match(navbar, /resolveNotificationPath\(notification\)/);
});

test('mark-read persistence cannot block navigation', () => {
  assert.match(navbar, /void markNotificationRead\(notification\.notificationId\)\.catch/);
  assert.ok(navbar.indexOf('navigate(path);') < navbar.indexOf('void markNotificationRead(notification.notificationId)'));
});

test('outside click and Escape close the notification dropdown', () => {
  assert.match(navbar, /document\.addEventListener\('mousedown', closeOnOutsideClick\)/);
  assert.match(navbar, /event\.key === 'Escape'/);
  assert.match(navbar, /notificationShellRef\.current\?\.contains\(event\.target\)/);
});

test('responsive dropdown stays within the viewport', () => {
  assert.match(styles, /width:\s*min\(400px,\s*calc\(100vw\s*-\s*24px\)\)/);
  assert.match(styles, /\.notification-panel\s*\{[^}]*right:\s*0/);
  assert.match(styles, /\.notification-panel\s*\{[^}]*z-index:\s*1000/);
});
