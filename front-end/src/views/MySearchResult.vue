<template>
  <div>
    <div v-for="result in searchResult" :key="result">
      {{ result }}
    </div>
  </div>
</template>

<script>
import ky from "ky";
export default {
  data() {
    return {
      searchResult: [],
    };
  },
  methods: {
    search(query) {
      console.log(query);
      this.searchResult = [
        "https://www.encyclopedia.com/places/britain-ireland-france-and-low-countries/british-and-irish-political-geography/glasgow",
        "https://www.encyclopedia.com/women/encyclopedias-almanacs-transcripts-and-maps/glasgow-ellen-1873-1945",
        "https://www.encyclopedia.com/reference/encyclopedias-almanacs-transcripts-and-maps/glasgow-university",
        "https://www.encyclopedia.com/caregiving/dictionaries-thesauruses-pictures-and-press-releases/glasgow-scoring-system",
        "https://www.encyclopedia.com/history/encyclopedias-almanacs-transcripts-and-maps/alfred-glasgow-encounter",
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

<style></style>
