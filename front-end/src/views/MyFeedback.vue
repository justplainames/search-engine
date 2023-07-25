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
      <h2>Please tell us which links are useful</h2>
      <div class="loading-results" v-if="!loaded">
        <my-spinner />
        <p>We are getting your results!</p>
      </div>
      <div
        v-for="(result, index) in searchResult"
        :key="index"
        class="results-wrapper"
      >
        <label class="checkbox-container">
          <input type="checkbox" @change="checkboxClicked($event, index)" />
          <span class="checkmark"></span>
        </label>
        <div @click="redirectToLink(result.url)">
          <h2 class="results-title">{{ result.title }}</h2>
          <p>{{ result.description }}</p>
          <hr />
        </div>
      </div>
    </div>
    <div class="button-container">
      <button @click="submitFeedback()">Submit Feedback</button>
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
      loaded: false,
      totalPages: 10,
      currentPage: 1,
      feedback: true,
    };
  },
  methods: {
    submitFeedback() {
      this.$router.push({
        name: "Search Result",
        params: { query: this.searchQuery, feedback: this.searchResult },
      });
    },
    checkboxClicked(event, index) {
      this.searchResult[index]["checked"] = event.target.checked;
    },
    selectedPage(page) {
      this.currentPage = page;
    },
    searchClicked() {
      console.log("search clicked");
    },
    async search() {
      const res = await this.$http
        .get("query?query=" + this.searchQuery)
        .json();
      if (res.error) {
        console.log(res.error);
      } else {
        this.searchResult = res;
      }
      this.loaded = true;
    },
    redirectToLink(url) {
      window.open(url, "_blank");
    },
  },
  mounted() {
    this.searchQuery = this.$route.params.query;
    this.search();
  },
};
</script>

<style lang="scss" scoped>
.search-results-section {
  height: 100vh;
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
  height: 100%;
  overflow-y: scroll;
  padding: 10px 50px 050px 50px;
}
.results-wrapper {
  text-align: left;
  padding-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
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
.checkbox-container {
  margin-right: 20px;

  input {
    height: 20px;
    width: 20px;
  }
}
.button-container {
  padding: 50px;
  button {
    cursor: pointer;
    border-radius: 5px;
    padding: 10px 20px;
    border: none;
    &:hover {
      background-color: rgb(172, 172, 172);
      color: white;
    }
  }
}
.search-logo {
  height: 50px;
}
</style>
