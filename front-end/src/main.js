import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import ky from "ky";

Vue.config.productionTip = false;

const api = ky.create({
  prefixUrl: "http://localhost:8080/",
  throwHttpErrors: false,
});

Vue.prototype.$http = api;
new Vue({
  router,
  render: (h) => h(App),
}).$mount("#app");
