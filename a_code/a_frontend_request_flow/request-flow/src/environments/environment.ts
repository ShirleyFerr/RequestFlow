export const environment = {
  production: false,
  apiUrl: window.location.port === '4200' ? 'http://localhost:8080/api' : '/api',
};
