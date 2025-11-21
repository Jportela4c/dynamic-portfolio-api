window.onload = function() {
  // Fix for OAuth2 Authorization Code flow logout/re-login issue
  // See: https://github.com/swagger-api/swagger-ui/issues/6034
  const ClearAuthCodePlugin = function() {
    return {
      statePlugins: {
        auth: {
          wrapActions: {
            authorizeOauth2: (oriAction, system) => (payload) => {
              // Clear the old authorization code before new authorization
              payload.auth.code = "";
              return oriAction(payload);
            }
          }
        }
      }
    };
  };

  window.ui = SwaggerUIBundle({
    url: "/api/v1/v3/api-docs",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl,
      ClearAuthCodePlugin
    ],
    layout: "StandaloneLayout",
    configUrl: "/api/v1/v3/api-docs/swagger-config"
  });
};
