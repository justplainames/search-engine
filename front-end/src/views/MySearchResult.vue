<template>
  <div class="search-results-section">
    <div class="search-results-container">
      <form @submit.prevent="onSubmit" class="form">
        <div class="form-content">
          <h1>GOGGLE</h1>
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
      <div
        v-for="(result, index) in searchResult"
        :key="index"
        class="results-wrapper"
      >
        <h2>{{ result.title }}</h2>
        <p>{{ result.description }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import ky from "ky";
export default {
  data() {
    return {
      searchResult: [],
      searchQuery: "",
    };
  },
  methods: {
    searchClicked() {
      console.log("search clicked");
    },
    search(query) {
      console.log(query);
      this.searchResult = [
        {
          title: "This is a good title",
          url: "https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
          description:
            "This is the description of this website. it is a good website as it contains many information about things and other things",
        },
        {
          title: "This is a good title",
          url: "https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
          description:
            "This is the description of this website. it is a good website as it contains many information about things and other things",
        },
        {
          title: "This is a good title",
          url: "https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
          description:
            "This is the description of this website. it is a good website as it contains many information about things and other things",
        },
        {
          title: "This is a good title",
          url: "https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
          description:
            "This is the description of this website. it is a good website as it contains many information about things and other things",
        },
        {
          title: "This is a good title",
          url: "https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
          description:
            "This is the description of this website. it is a good website as it contains many information about things and other things",
        },
        {
          title: "This is a good title",
          url: "https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
          description:
            "This is the description of this website. it is a good website as it contains many information about things and other things",
        },
      ];
    },
    httpRequest(success, error) {
      const http = new XMLHttpRequest();
      const params = "url=" + this.url;
      http.open("POST", this.apiUrl, true);
      http.setRequestHeader(
        "Content-type",
        "application/x-www-form-urlencoded"
      );
      http.onreadystatechange = function () {
        if (http.readyState === 4 && http.status === 200) {
          success(http.responseText);
        }
        if (http.readyState === 4 && http.status === 500) {
          error();
        }
      };
      http.send(params);
    },
  },
  mounted() {
    this.search(this.$route.params.query);
    const res = ky
      .get(
        "https://jsonlink.io/api/extract?url=https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      )
      .json();
    console.log(res);
  },
};
</script>

<style lang="scss" scoped>
.search-results-section {
  min-height: Calc(100vh - 100px);
  width: Calc(100vw - 100px);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50px;
}
.search-results-container {
  max-width: 1400px;
}
.results-wrapper {
  text-align: left;
  margin-bottom: 35px;
  h2 {
    margin-bottom: 0;
  }
  p {
    margin-top: 10px;
  }
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
</style>
