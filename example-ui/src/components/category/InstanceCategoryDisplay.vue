<script lang="ts">
import { defineComponent } from 'vue';
import { SelectionType, type Node } from '@/types/categoryGraph';
import type { Graph } from '@/types/categoryGraph';
import InstanceObject from './InstanceObject.vue';
import GraphDisplay from './GraphDisplay.vue';
import type { SchemaObject } from '@/types/schema';

export default defineComponent({
    components: {
        GraphDisplay,
        InstanceObject
    },
    data() {
        return {
            graph: null as Graph | null,
            selectedNode: null as Node | null
        };
    },
    methods: {
        cytoscapeCreated(graph: Graph) {
            this.graph = graph;

            graph.addNodeListener('tap', node => this.selectNode(node));
        },
        objectClicked(object: SchemaObject) {
            const newNode = this.graph?.getNode(object);
            if (newNode)
                this.selectNode(newNode);
        },
        selectNode(node: Node) {
            this.selectedNode?.unselect();
            this.selectedNode = node;
            this.selectedNode.select({ type: SelectionType.Root, level: 0 });
        }
    }
});
</script>

<template>
    <div class="divide">
        <GraphDisplay @create:graph="cytoscapeCreated" />
        <InstanceObject
            v-if="selectedNode"
            :node="selectedNode"
            @object:click="objectClicked"
        />
    </div>
</template>

<style scoped>

</style>
