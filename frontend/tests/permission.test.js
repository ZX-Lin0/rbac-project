import test from 'node:test';
import assert from 'node:assert/strict';

function checkPermission(permissions, code) {
  if (!permissions || permissions.length === 0) {
    return false;
  }
  const matchSingle = (p) => {
    if (permissions.includes(p)) return true;
    if (p === 'user:update' && permissions.includes('user:upadate')) return true;
    if (p === 'user:upadate' && permissions.includes('user:update')) return true;
    return false;
  };
  if (Array.isArray(code)) {
    return code.some((item) => matchSingle(item));
  }
  return matchSingle(code);
}

test('normal user with only user:list has no role:list permission', () => {
  const normalUserPerms = ['user:list'];
  assert.equal(checkPermission(normalUserPerms, 'user:list'), true);
  assert.equal(checkPermission(normalUserPerms, 'role:list'), false);
  assert.equal(checkPermission(normalUserPerms, 'user:assign'), false);
});

test('admin user with role:list and user:assign can access role operations', () => {
  const adminPerms = ['user:list', 'user:assign', 'role:list', 'role:assign'];
  assert.equal(checkPermission(adminPerms, 'user:list'), true);
  assert.equal(checkPermission(adminPerms, 'role:list'), true);
  assert.equal(checkPermission(adminPerms, 'user:assign'), true);
});

test('handles empty or missing permissions gracefully', () => {
  assert.equal(checkPermission([], 'role:list'), false);
  assert.equal(checkPermission(null, 'role:list'), false);
  assert.equal(checkPermission(undefined, 'role:list'), false);
});

test('supports compatibility alias for user:update and user:upadate', () => {
  assert.equal(checkPermission(['user:upadate'], 'user:update'), true);
  assert.equal(checkPermission(['user:update'], 'user:upadate'), true);
});
