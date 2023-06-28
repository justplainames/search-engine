import Vue from "vue";
import VueRouter from "vue-router";
import MySearch from "../views/MySearch.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Search",
    component: MySearch,
  },
  {
    path: "/search/:query",
    name: "Search Result",
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/MySearchResult.vue"),
  },
  //   {
  //     path: "/about",
  //     name: "About Us",
  //     component: () =>
  //       import(/* webpackChunkName: "about" */ "../views/AboutUs.vue"),
  //   },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
  scrollBehavior: function (to) {
    if (to.hash) {
      return {
        selector: to.hash,
        behavior: "smooth",
      };
      //Or for Vue 3:
      //return {el: to.hash}
    } else {
      return { x: 0, y: 0 };
    }
  },
});
export default router;
