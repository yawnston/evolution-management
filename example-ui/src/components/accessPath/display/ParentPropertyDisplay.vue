<script lang="ts">
import { SimpleProperty, ComplexProperty, type ParentProperty, RootProperty } from '@/types/accessPath/graph';
import { SimpleProperty as BasicSimpleProperty, ComplexProperty as BasicComplexProperty, type ParentProperty as BasicParentProperty } from '@/types/accessPath/basic';
import { defineComponent } from 'vue';
import SimplePropertyDisplay from './SimplePropertyDisplay.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';

export default defineComponent({
    name: 'ParentPropertyDisplay',
    components: {
        SimplePropertyDisplay,
        IconPlusSquare,
    },
    props: {
        property: {
            type: Object as () => ParentProperty | BasicParentProperty,
            required: true
        },
        isLast: {
            type: Boolean,
            default: true,
            required: false
        },
        isRoot: {
            type: Boolean,
            default: true,
            required: false
        },
        disableAdditions: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'complex:click', 'simple:click', 'add:click' ],
    data() {
        return {
            highlighted: false
        };
    },
    computed: {
        simpleSubpaths(): (SimpleProperty | BasicSimpleProperty)[] {
            return this.property instanceof RootProperty || this.property instanceof ComplexProperty ?
                this.property.subpaths.filter((subpath): subpath is SimpleProperty => subpath instanceof SimpleProperty) :
                this.property.subpaths.filter((subpath): subpath is BasicSimpleProperty => subpath instanceof BasicSimpleProperty);
        },
        complexSubpaths(): (ComplexProperty | BasicComplexProperty)[] {
            return this.property instanceof RootProperty || this.property instanceof ComplexProperty ?
                this.property.subpaths.filter((subpath): subpath is ComplexProperty => subpath instanceof ComplexProperty) :
                this.property.subpaths.filter((subpath): subpath is BasicComplexProperty => subpath instanceof BasicComplexProperty);
        }
    },
    methods: {
        reEmitComplexClick(property: ComplexProperty): void {
            this.$emit('complex:click', property);
        },
        reEmitSimpleClick(property: SimpleProperty): void {
            this.$emit('simple:click', property);
        },
        reEmitAddClick(property: ComplexProperty): void {
            this.$emit('add:click', property);
        },
        emitComplexClick(): void {
            if (!this.isRoot)
                this.$emit('complex:click', this.property);
        }
    }
});
</script>


<template>
    <div class="outer">
        <div class="row">
            <span
                class="name-text"
                :class="{ highlighted, clickable: !isRoot }"
                @click="emitComplexClick"
                @mouseenter="highlighted = true;"
                @mouseleave="highlighted = false"
            >
                {{ property.name }}: {{ property.isAuxiliary ? '' : (property.signature + ' ') }}{
            </span>
        </div>
        <div class="property-divide">
            <div class="filler">
                <div
                    class="filler-line"
                    :class="{ highlighted }"
                />
            </div>
            <div class="inner">
                <SimplePropertyDisplay
                    v-for="(subpath, index) in simpleSubpaths"
                    :key="index"
                    :property="subpath"
                    :is-last="index === property.subpaths.length - 1"
                    @simple:click="reEmitSimpleClick"
                />
                <ParentPropertyDisplay
                    v-for="(subpath, index) in complexSubpaths"
                    :key="index"
                    :property="subpath"
                    :is-last="index === complexSubpaths.length - 1"
                    :is-root="false"
                    :disable-additions="disableAdditions"
                    @complex:click="reEmitComplexClick"
                    @simple:click="reEmitSimpleClick"
                    @add:click="reEmitAddClick"
                />
                <span
                    v-if="!disableAdditions"
                    class="button-icon"
                    @click="$emit('add:click', property)"
                    @mouseenter="highlighted = true;"
                    @mouseleave="highlighted = false"
                >
                    <IconPlusSquare />
                </span>
            </div>
        </div>
        <div class="row">
            <span
                ref="bracketText"
                class="bracket-text"
                :class="{ highlighted, clickable: !isRoot }"
                @click="emitComplexClick"
                @mouseenter="highlighted = true"
                @mouseleave="highlighted = false"
            >
                }{{ isLast ? '' : ',' }}
            </span>
        </div>
    </div>
</template>

<style scoped>
.outer {
    display: flex;
    flex-direction: column;
}

.property-divide {
    display: flex;
    flex-direction: row;
}

.filler {
    width: 32px;
    padding-left: 3px;
    padding-top: 6px;
    padding-bottom: 6px;
}

.filler-line {
    width: 6px;
    height: 100%;
    border-radius: 3px;
}

.inner {
    display: flex;
    flex-direction: column;
}

.highlighted {
    background-color: var(--color-background-dark);
}

.name-text, .bracket-text {
    width: fit-content;
    padding: 2px 4px;
    border-radius: 4px;
}

.clickable {
    cursor: pointer;
}
</style>
