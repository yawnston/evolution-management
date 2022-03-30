<script lang="ts">
import type { NodeSchemaData } from '@/types/categoryGraph';
import { DynamicName, Signature, StaticName, type Name } from '@/types/identifiers';
import type { Core } from 'cytoscape';
import { defineComponent } from 'vue';
import SignatureInput from './SignatureInput.vue';

enum NameType {
    Static,
    Dynamic,
    Anonymous
}

export default defineComponent({
    components: { SignatureInput },
    props: {
        cytoscape: {
            type: Object as () => Core,
            required: true
        },
        rootNode: {
            type: Object as () => NodeSchemaData,
            required: true
        },
        modelValue: {
            type: Object as () => Name,
            required: true
        },
        disabled: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: this.modelValue,
            type: this.getNameType(this.modelValue),
            staticValue: this.modelValue instanceof StaticName && !this.modelValue.isAnonymous ? this.modelValue.value : '',
            dynamicValue: this.modelValue instanceof DynamicName ? this.modelValue.signature : Signature.empty,
            NameType
        };
    },
    watch: {
        modelValue: {
            handler(newValue: Name): void {
                if (!newValue.equals(this.innerValue as Name)) {
                    this.innerValue = this.modelValue;
                    this.type = this.getNameType(this.modelValue);
                    this.staticValue = this.modelValue instanceof StaticName && !this.modelValue.isAnonymous ? this.modelValue.value : '';
                    this.dynamicValue = this.modelValue instanceof DynamicName ? this.modelValue.signature : Signature.empty;
                }
            }
        }
    },
    methods: {
        getNameType(name: Name): NameType {
            return name instanceof StaticName
                ? (name.isAnonymous ? NameType.Anonymous : NameType.Static)
                : NameType.Dynamic;
        },
        updateInnerValue() {
            switch (this.type) {
            case NameType.Static:
                this.innerValue = StaticName.fromString(this.staticValue);
                break;
            case NameType.Dynamic:
                this.innerValue = DynamicName.fromSignature(this.dynamicValue as Signature);
                break;
            case NameType.Anonymous:
                this.innerValue = StaticName.anonymous;
                break;
            }

            this.$emit('update:modelValue', this.innerValue);
        }
    }
});
</script>

<template>
    <div class="outer">
        <input
            id="static"
            v-model="type"
            type="radio"
            :value="NameType.Static"
            :disabled="disabled"
            @change="updateInnerValue"
        />
        <label for="static">Static</label><br />
        <input
            id="dynamic"
            v-model="type"
            type="radio"
            :value="NameType.Dynamic"
            :disabled="disabled"
            @change="updateInnerValue"
        />
        <label for="dynamic">Dynamic</label><br />
        <input
            id="anonymous"
            v-model="type"
            type="radio"
            :value="NameType.Anonymous"
            :disabled="disabled"
            @change="updateInnerValue"
        />
        <label for="anonymous">Anonymous</label><br />
        <div v-if="type === NameType.Static">
            <input
                v-model="staticValue"
                @input="updateInnerValue"
            />
        </div>
        <div v-if="type === NameType.Dynamic">
            <SignatureInput
                v-model="dynamicValue"
                :cytoscape="cytoscape"
                :root-node="rootNode"
                :disabled="disabled"
                @input="updateInnerValue"
            />
        </div>
    </div>
</template>

<style scoped>
.outer {
    background-color: darkgreen;
    padding: 12px;
}
</style>
