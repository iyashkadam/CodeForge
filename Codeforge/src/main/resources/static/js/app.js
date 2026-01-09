// CodeForge - Core Application JavaScript

const API_BASE = 'http://localhost:8080';

// ==================== Token Management ====================

function getToken() {
  return localStorage.getItem('token');
}

function getRole() {
  return localStorage.getItem('role');
}

function setAuth(token, role) {
  localStorage.setItem('token', token);
  localStorage.setItem('role', role);
}

function clearAuth() {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
}

function isAuthenticated() {
  return !!getToken();
}

// ==================== API Fetch Wrapper ====================

async function apiFetch(endpoint, options = {}) {
  const token = getToken();

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers
    });

    // Handle auth errors
    if (response.status === 401 || response.status === 403) {
      clearAuth();
      window.location.href = 'index.html';
      throw new Error('Unauthorized');
    }

    // Try to parse JSON
    const contentType = response.headers.get('content-type');
    let data = null;

    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      throw new Error(data.message || data || 'Request failed');
    }

    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}

// ==================== Auth Functions ====================

async function login(email, password) {
  const data = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password })
  });

  if (data.token) {
    // Decode JWT to get role
    const payload = JSON.parse(atob(data.token.split('.')[1]));
    const role = payload.role || 'STUDENT';
    setAuth(data.token, role);
    return { success: true, role };
  }

  throw new Error('Invalid response from server');
}

async function register(name, email, password, branch) {
  return await apiFetch('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ name, email, password, branch })
  });
}

function logout() {
  clearAuth();
  window.location.href = 'index.html';
}

// ==================== Role-Based Access ====================

function requireAuth() {
  if (!isAuthenticated()) {
    window.location.href = 'index.html';
    return false;
  }
  return true;
}

function requireRole(requiredRole) {
  if (!requireAuth()) return false;

  const role = getRole();
  if (role !== requiredRole) {
    // Redirect to appropriate dashboard
    if (role === 'ADMIN') {
      window.location.href = 'admin-dashboard.html';
    } else {
      window.location.href = 'dashboard.html';
    }
    return false;
  }
  return true;
}

function redirectIfAuthenticated() {
  if (isAuthenticated()) {
    const role = getRole();
    if (role === 'ADMIN') {
      window.location.href = 'admin-dashboard.html';
    } else {
      window.location.href = 'dashboard.html';
    }
    return true;
  }
  return false;
}

// ==================== Course Functions ====================

async function getCourses() {
  return await apiFetch('/api/courses');
}

async function createCourse(courseData) {
  return await apiFetch('/api/courses/create', {
    method: 'POST',
    body: JSON.stringify(courseData)
  });
}

// ==================== Enrollment Functions ====================

async function enrollInCourse(courseId) {
  return await apiFetch(`/api/enrollments/${courseId}`, {
    method: 'POST'
  });
}

async function getMyEnrollments() {
  return await apiFetch('/api/enrollments/my');
}

async function unenrollFromCourse(courseId) {
  return await apiFetch(`/api/enrollments/${courseId}`, {
    method: 'DELETE'
  });
}

async function getEnrolledStudents(courseId) {
  return await apiFetch(`/api/enrollments/course/${courseId}`);
}

// ==================== Chat Functions ====================

let chatRefreshInterval = null;
let currentUserEmail = null;

function decodeToken() {
  const token = getToken();
  if (!token) return null;
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
}

async function getChatMessages(courseId) {
  return await apiFetch(`/api/chat/${courseId}`);
}

async function sendChatMessage(courseId, content) {
  try {
    await apiFetch(`/api/chat/${courseId}`, {
      method: 'POST',
      body: JSON.stringify({ content })
    });
    return true;
  } catch (e) {
    console.error('Send message error:', e);
    return false;
  }
}

function startChatRefresh(courseId, containerId) {
  currentUserEmail = decodeToken()?.sub || decodeToken()?.email || '';
  loadChatMessages(courseId, containerId);
  chatRefreshInterval = setInterval(() => loadChatMessages(courseId, containerId), 3000);
}

function stopChatRefresh() {
  if (chatRefreshInterval) {
    clearInterval(chatRefreshInterval);
    chatRefreshInterval = null;
  }
}

async function loadChatMessages(courseId, containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;

  try {
    const messages = await getChatMessages(courseId);
    renderChatMessages(messages, containerId);
  } catch (e) {
    // Chat API may not exist yet, show empty state
    container.innerHTML = `
      <div class="empty-state">
        <h3>No messages yet</h3>
        <p>Be the first to start the conversation!</p>
      </div>
    `;
  }
}

function renderChatMessages(messages, containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (!messages || messages.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <h3>No messages yet</h3>
        <p>Be the first to start the conversation!</p>
      </div>
    `;
    return;
  }

  container.innerHTML = messages.map(msg => {
    const senderEmail = msg.senderEmail || msg.sender?.email || '';
    const senderName = msg.senderName || msg.sender?.name || 'Unknown';
    const isOwn = senderEmail === currentUserEmail;
    const timestamp = msg.timestamp ? formatTime(msg.timestamp) : '';

    return `
      <div class="chat-message ${isOwn ? 'own' : 'other'}">
        <div class="message-bubble">${escapeHtml(msg.content)}</div>
        <div class="message-meta">
          ${!isOwn ? `<span class="message-sender">${escapeHtml(senderName)}</span>` : ''}
          <span>${timestamp}</span>
        </div>
      </div>
    `;
  }).join('');

  container.scrollTop = container.scrollHeight;
}

// ==================== Community Functions ====================

async function loadCommunityMembers(courseId, containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;

  try {
    const members = await getEnrolledStudents(courseId);

    if (!members || members.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <h3>No members yet</h3>
          <p>Be the first to join!</p>
        </div>
      `;
      return;
    }

    container.innerHTML = members.map(member => {
      const student = member.student || member;
      const name = student.name || 'Unknown';
      const email = student.email || '';
      const branch = student.branch || '';
      const initials = name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

      return `
        <div class="member-item">
          <div class="member-avatar">${initials}</div>
          <div class="member-info">
            <div class="member-name">${escapeHtml(name)}</div>
            <div class="member-email">${escapeHtml(email)}</div>
            ${branch ? `<span class="member-branch">${escapeHtml(branch)}</span>` : ''}
          </div>
        </div>
      `;
    }).join('');
  } catch (e) {
    container.innerHTML = `<div class="message message-error">Failed to load members</div>`;
  }
}

async function checkEnrollment(courseId) {
  try {
    const enrollments = await getMyEnrollments();
    return enrollments.some(e => (e.courseId || e.course?.id) == courseId);
  } catch (e) {
    return false;
  }
}

// ==================== Search Functions ====================

function filterCourses(courses, searchTerm) {
  if (!searchTerm || searchTerm.trim() === '') return courses;
  const term = searchTerm.toLowerCase().trim();
  return courses.filter(c =>
    (c.title || c.name || '').toLowerCase().includes(term) ||
    (c.description || '').toLowerCase().includes(term)
  );
}

// ==================== UI Helpers ====================

function showMessage(containerId, message, type = 'info') {
  const container = document.getElementById(containerId);
  if (container) {
    container.innerHTML = `<div class="message message-${type}">${escapeHtml(message)}</div>`;
    setTimeout(() => { container.innerHTML = ''; }, 5000);
  }
}

function showAlert(containerId, message, type = 'info') {
  showMessage(containerId, message, type);
}

function showLoading(containerId) {
  const container = document.getElementById(containerId);
  if (container) {
    container.innerHTML = '<div class="loading">Loading...</div>';
  }
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function formatDate(dateString) {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric'
  });
}

function formatTime(dateString) {
  if (!dateString) return '';
  return new Date(dateString).toLocaleTimeString('en-US', {
    hour: '2-digit', minute: '2-digit'
  });
}

function getUrlParam(param) {
  return new URLSearchParams(window.location.search).get(param);
}

// ==================== Input Validation ====================

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validatePassword(password) {
  return password && password.length >= 6;
}

function validateRequired(value) {
  return value && value.trim().length > 0;
}

// ==================== Initialization ====================

document.addEventListener('DOMContentLoaded', () => {
  const logoutBtns = document.querySelectorAll('.btn-logout, #logoutBtn');
  logoutBtns.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      logout();
    });
  });
});
