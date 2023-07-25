<template>
  <div class="search-results-section">
    <div class="search-bar-container">
      <form @submit.prevent="onSubmit" class="form">
        <div class="form-content">
          <router-link to="/">
            <img
              src="@/assets/goggle-text-side-by-side.png"
              class="search-logo"
          /></router-link>
          <input
            type="text"
            id="searchQuery"
            class="input--pill"
            v-model="searchQuery"
          />
          <img
            role="img"
            src="../assets/search.svg"
            class="search-icon"
            @click="searchClicked"
          />
        </div>
      </form>
    </div>
    <hr />
    <div class="search-results-container">
      <div class="loading-results" v-if="!loaded">
        <my-spinner />
        <p>We are getting your results!</p>
      </div>
      <div
        v-for="(result, index) in searchResult"
        :key="index"
        class="results-wrapper"
        @click="redirectToLink(result.url)"
      >
        <h2 class="results-title">{{ result.title }}</h2>
        <p>{{ result.description }}</p>
        <hr />
      </div>
    </div>
  </div>
</template>

<script>
import MySpinner from "../components/MySpinner.vue";
export default {
  components: { MySpinner },
  data() {
    return {
      searchResult: [],
      searchQuery: "",
      searchFeedback: [],
      loaded: false,
      totalPages: 10,
      currentPage: 1,
    };
  },
  methods: {
    selectedPage(page) {
      this.currentPage = page;
    },
    searchClicked() {
      if (this.searchQuery != null) {
        this.$router.push({
          name: "Feedback",
          params: { query: this.searchQuery },
        });
      }
    },

    redirectToLink(url) {
      window.open(url, "_blank");
    },
    async sendFeedback() {
      var payload = this.searchFeedback.map((obj) => obj.url);
      const res = await this.$http
        .post("urlQuery/", {
          json: payload,
        })
        .json();
      if (res.error) {
        console.log(res.error);
      } else {
        this.searchResult = res;
      }
      this.loaded = true;
    },
  },
  mounted() {
    this.searchQuery = this.$route.params.query;
    this.searchFeedback = this.$route.params.feedback;
    this.sendFeedback();
  },
};
</script>

<style lang="scss" scoped>
.search-results-section {
  min-height: 100vh;
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.search-bar-container {
  max-width: 1400px;
  height: 200px;
  margin-top: 20px;
}
.search-results-container {
  max-width: 1400px;
  padding: 10px 50px 050px 50px;
}
.results-wrapper {
  text-align: left;
  padding-top: 20px;
  h2 {
    margin-bottom: 0;
  }
  p {
    margin-top: 10px;
  }
  &:hover {
    background-color: rgb(247, 247, 247);
  }
}
.results-title {
  cursor: pointer;
  position: relative;
  width: max-content;
  color: #0645ad;
  &::after {
    content: "";
    position: absolute;
    width: 100%;
    transform: scaleX(0);
    height: 2px;
    bottom: 0;
    left: 0;
    background-color: #0861f0;
    transform-origin: bottom right;
    transition: transform 0.25s ease-out;
  }
  &:hover::after {
    transform: scaleX(1);
    transform-origin: bottom left;
  }
  &:hover {
    color: #0861f0;
  }
  // &:hover{
  //   text-decoration: underline;
  // }
}
.form {
  width: 100%;
  margin-top: 20px;
  display: flex;
  justify-content: flex-start;
}
.form-content {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: row;
  position: relative;
}

.search-icon {
  height: 15px;
  width: 15px;
  position: absolute;
  right: 12px;
  cursor: pointer;
}
.input--pill {
  width: 500px;
  border-radius: 20px;
  border: 1px solid black;
  padding: 10px;
  margin-left: 20px;
}
hr {
  border: 0;
  clear: both;
  display: block;
  width: 100%;
  background-color: #5e5e5e;
  height: 1px;
  margin: 0;
  padding: 0;
}
.router-link,
.router-link-active {
  text-decoration: none;
  color: black;
}
.results-amount {
  text-align: left;
}
.sub-text {
  color: rgb(71, 71, 71);
}
.search-logo {
  height: 50px;
}
</style>
