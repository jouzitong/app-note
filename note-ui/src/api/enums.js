import { createHttp } from "@/api/http-client";

const server = "/common/v1/system/enums";
const $http = createHttp(server);

const api = {
  fetchGlobalEnums: async function () {
    const body = await $http.get("");
    if (body && typeof body === "object") {
      return body;
    }
    return {};
  },
};

export default api;
