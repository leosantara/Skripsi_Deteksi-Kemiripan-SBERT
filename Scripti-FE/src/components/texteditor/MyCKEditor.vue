<template>
    <div>
      <ckeditor
        :editor="editor"
        v-model="editorData"
        :config="editorConfig"
        :disabled="props.disabled"
        :placeholder="props.placeholder"
        @ready="onReady"
        tag-name="textarea"
      />
      <span v-if="errorMessage" class="warn">{{ errorMessage }}</span>
    </div>
</template>

<script setup>
import { defineModel, watch } from 'vue';
import { useField } from 'vee-validate';
import { defineProps, defineEmits } from 'vue';

import 'ckeditor5/ckeditor5.css';
import { Ckeditor }  from '@ckeditor/ckeditor5-vue';
import { ClassicEditor, Bold, Essentials, Italic, Mention, Paragraph, Undo, Heading, Link, Underline, List, Indent, Superscript, Subscript, Alignment, Clipboard} from 'ckeditor5';


// Register the CKEditor component
const editor = ClassicEditor;
const editorData = defineModel('editorData', {default:null});
const editorConfig = {
    plugins: [ Bold, Essentials, Italic, Mention, Paragraph, Undo, Link, Heading, Underline, List, Superscript, Subscript, Indent, Alignment, Clipboard ],
    toolbar: [ 
        'undo', 'redo', '|', 
        'bold', 'italic', 'underline', 'superscript', 'subscript', '|', 
        'link', 'bulletedList', 'numberedList', '|',
        'heading', 'alignment', 'outdent', 'indent'
    ],
    shouldNotGroupWhenFull: false
};

const emits = defineEmits(['update:modelValue'])

const props = defineProps({
    name: String,
    placeholder: String,
    modelValue: String,
    disabled: Boolean
})

const {value, errorMessage, setValue} = useField(
    () => props.name,
    undefined,
    {syncVModel:true}
)


// we must connect VALUE from vee-validate to editordata from ckeditor5 to trigger validation:
watch(
    () => editorData.value,
    (newVal, oldVal) => {
        setValue(newVal)
    }
)
// initializing the value to be validated by vee-validate
const onReady = (editor) => {
    setValue(editorData.value)
}
</script>

<style scoped>
.warn{
    color: crimson;
}
.ck .ck-editor__main > .ck-editor__editable.ck-read-only {
  background: #C3C3C3;
}
</style>