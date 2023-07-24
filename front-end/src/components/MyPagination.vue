<template>
  <div class="pagination">
    <div
      class="pagination-action"
      @click="
        () => {
          if (this.currentPage > 1) {
            this.$emit('previousPage');
          }
        }
      "
    >
      <span>Before</span>
    </div>
    <div
      v-for="index in pages"
      :key="index"
      class="pagination-index"
      :class="[currentPage == index ? 'active' : '']"
      @click="$emit('selectedPage', index)"
    >
      <span>{{ index }}</span>
    </div>
    <div
      class="pagination-action"
      @click="
        () => {
          if (this.currentPage < this.pages) {
            this.$emit('nextPage');
          }
        }
      "
    >
      <span>Next</span>
    </div>
  </div>
</template>

<script>
export default {
  props: ["currentPage", "pages"],
};
</script>

<style lang="scss" scoped>
.pagination {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 20px;
  cursor: pointer;
}
.pagination-index {
  color: rgb(94, 94, 94);
  width: 15px;
  height: 20px;
  padding: 15px;
  text-decoration: none;
  border-radius: 5px;
  font-size: 18px;
  &:hover {
    background-color: rgb(209, 209, 209);
  }
  &.active {
    text-decoration: underline;
    font-weight: 700;
    color: black;
  }
}
.pagination-action {
  border-radius: 5px;
  padding: 10px;
  margin: 20px;
  border: 1px solid rgb(143, 143, 143);
  &:hover {
    background-color: rgb(143, 143, 143);
    color: white;
  }
}
</style>
