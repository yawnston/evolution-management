<script setup lang="ts">
import { RouterView } from 'vue-router';
</script>

<template>
    <main class="outerContent">
        <div class="innerContent">
            <RouterView />
        </div>
    </main>
</template>

<style scoped>
.outerContent {
    padding-top: var(--top-bar-height);
    padding-left: var(--left-panel-width);
}

.innerContent {
    padding: 10px 20px;
}
</style>
