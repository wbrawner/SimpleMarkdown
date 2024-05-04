#include <jni.h>
#include "md4c.h"
#include "md4c-html.h"
#include <stdlib.h>
#include <string.h>

typedef struct {
    char *data;
    size_t length;
} HtmlBuffer;

void process_output(const MD_CHAR *output, MD_SIZE size, void *userdata) {
    HtmlBuffer *buffer = (HtmlBuffer *) userdata;
    buffer->data = realloc(buffer->data, buffer->length + size);
    memcpy(buffer->data + buffer->length, output, size);
    buffer->length += size;
}

jstring Java_com_wbrawner_md4k_MD4K_toHtml(
        JNIEnv *env,
        jobject this,
        jstring markdown
) {
    const char *nativeString = (*env)->GetStringUTFChars(env, markdown, NULL);
    HtmlBuffer buffer;
    buffer.data = malloc(0);
    buffer.length = 0;
    md_html(
            nativeString,
            strlen(nativeString),
            &process_output,
            &buffer,
            MD_FLAG_PERMISSIVEAUTOLINKS | MD_FLAG_TABLES | MD_FLAG_STRIKETHROUGH |
            MD_FLAG_TASKLISTS | MD_FLAG_LATEXMATHSPANS | MD_FLAG_WIKILINKS | MD_FLAG_UNDERLINE,
            0
    );
    (*env)->ReleaseStringUTFChars(env, markdown, nativeString);
    jstring html = (*env)->NewStringUTF(env, buffer.data);
    free(buffer.data);
    return html;
}