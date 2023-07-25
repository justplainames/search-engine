<template>
  <div class="section">
    <form @submit.prevent="onSubmit" class="form">
      <div class="form-content">
        <img src="@/assets/goggle-text-stacked.png" class="logo-img" />
        <input
          type="text"
          id="searchQuery"
          class="input--pill"
          v-model="searchQuery"
        />
        <button type="submit" class="button">SEARCH</button>
      </div>
    </form>
  </div>
</template>
<script>
export default {
  data() {
    return {
      searchQuery: "",
    };
  },
  methods: {
    onSubmit() {
      if (this.searchQuery) {
        this.$router.push({
          name: "Feedback",
          params: { query: this.searchQuery },
        });
      }
    },
  },
};
</script>
<style lang="scss" scoped>
.section {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}
.form {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}
.form-content {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 50%;
  max-width: 500px;
}
.button {
  margin-top: 20px;
  padding: 8px 20px;
  background-color: white;
  border: 1px solid rgb(185, 185, 185);
  border-radius: 5px;
  cursor: pointer;
  box-shadow: rgba(100, 100, 111, 0.2) 0px 7px 29px 0px;
  &:hover {
    background-color: rgb(230, 230, 230);
  }
}
.input--pill {
  width: 100%;
  border-radius: 20px;
  border: 1px solid black;
  padding: 10px;
}
.logo-img {
  height: 250px;
}
</style>
