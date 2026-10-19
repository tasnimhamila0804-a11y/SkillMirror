/**
 * Dev-server proxy: the Angular app always talks to the same origin (/api, /actuator),
 * the dev server forwards to the Spring Boot backend.
 *
 * Override the target when the backend runs on a custom port:
 *   SKILLMIRROR_API=http://localhost:8085 npm start
 */
const target = process.env.SKILLMIRROR_API ?? 'http://localhost:8080';

export default {
  '/api': {
    target,
    changeOrigin: true,
    secure: false,
  },
  '/actuator': {
    target,
    changeOrigin: true,
    secure: false,
  },
};
