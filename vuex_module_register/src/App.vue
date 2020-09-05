<template>
  <div id="app">
    <div>{{ $store.state.user.name }}</div>
    <div>{{ $store.state.post.title }}</div>
    <div>{{ $store.state.user.inner.name }}</div>
    <div>{{ $store.getters['user/getName'] }}</div>
    <div>{{ $store.getters['post/getTitle'] }}</div>
    <div>{{ $store.getters['user/inner/getName'] }}</div>
  </div>
</template>

<script>

export default {
  name: 'App',
}
</script>

<style>
</style>
