const defaultValue = "default";

// JsonSerializerOptionPlugin adds a select input for choosing a value for the X-Json-Serializer-Option header value to be used for subsequent requests
// from the swagger ui, and state to keep the selected value.
const JsonSerializerOptionPlugin = () => {
    return {
        statePlugins: {
            jsonSerializerOption: {
                actions: {
                    updateJsonSerializerOption: (option) => {
                        return {
                            type: "UPDATE_JSON_SERIALIZER_OPTION",
                            payload: option,
                        }
                    }
                },
                reducers: {
                    "UPDATE_JSON_SERIALIZER_OPTION": (state, action) => {
                        return state.set("xJsonSerializerOption", action.payload)
                    }
                },
                selectors: {
                    xJsonSerializerOption: (state) => state.get("xJsonSerializerOption")
                },
            }
        },
        wrapComponents: {
            ServersContainer: (Original, system) => props => {
                const React = system.React;
                const options = [defaultValue, "openapi-compat"].map(opt => React.createElement("option", {value: opt}, `${opt}`))
                const selectId = "JsonSerializerOptionSelect"
                const onChange = (ev) => {
                    system.jsonSerializerOptionActions.updateJsonSerializerOption(ev.target.value);
                }
                const select = React.createElement("select", {id: selectId, onChange}, ...options)
                const label = React.createElement("label", {for: selectId, style: {display: "block", marginBottom: "0", paddingTop: "4px"}}, "X-Json-Serializer-Option")
                const original = React.createElement(Original, props)
                const div = React.createElement("div", null, label, select)
                return [original, div]
            }
        }
    }
}

window.onload = function() {
  // Begin Swagger UI call region
  const ui = SwaggerUIBundle({
    url: "/k9/tilbake/api/openapi.json",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset,
    ],
    plugins: [
      JsonSerializerOptionPlugin,
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
    // requestInterceptor adds chosen X-Json-Serializer-Option header value to all requests, if it has been changed from default (undefined).
    requestInterceptor: (req) => {
        // On first request after load (to get openapi.yaml), this.ui is null.
        // serializerOptionVal would always be undefined (default value) anyway at that point, so doesn't matter.
        if(this.ui != null) {
            const serializerOptionVal = this.ui.jsonSerializerOptionSelectors.xJsonSerializerOption()
            if(serializerOptionVal != null && serializerOptionVal !== defaultValue) {
                req.headers["X-Json-Serializer-Option"] = "openapi-compat";
            }
        }
        return req
    }
  })
  // End Swagger UI call region

  window.ui = ui
}
