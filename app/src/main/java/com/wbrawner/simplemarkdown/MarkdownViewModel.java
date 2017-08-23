package com.wbrawner.simplemarkdown;

public class MarkdownViewModel {
/*
    private static final String TAG = MarkdownViewModel.class.getSimpleName();
    private File file;
    public MutableLiveData<String> markdownLiveData;
    private MutableLiveData<String> htmlLiveData = new MutableLiveData<>();;

    public MarkdownViewModel() {
        markdownLiveData = new MutableLiveData<>();
    }

    public void updateMarkdown(String data) {
        if (markdownLiveData == null)
            markdownLiveData = new MutableLiveData<>();
        markdownLiveData.postValue(data);
        Runnable generateMarkdown = () -> {
            AndDown andDown = new AndDown();
            int hoedownFlags =
                    AndDown.HOEDOWN_EXT_STRIKETHROUGH | AndDown.HOEDOWN_EXT_TABLES |
                            AndDown.HOEDOWN_EXT_UNDERLINE | AndDown.HOEDOWN_EXT_SUPERSCRIPT |
                            AndDown.HOEDOWN_EXT_FENCED_CODE;
            htmlLiveData.postValue(andDown.markdownToHtml(markdownLiveData.getValue(), hoedownFlags, 0));
        };
        if (markdownLiveData.getValue() != null)
            generateMarkdown.run();
    }

    public LiveData<String> getHtml() {
        return htmlLiveData;
    }

    public void openFile(String filePath) {
        file = new File(filePath);
    }

    public String getFileName() {
        if (file == null || file.getName().isEmpty())
            return "Untitled.md";
        return file.getName();
    }

    public boolean saveFile(String filePath, @Nullable String fileName) {
        if (fileName == null) {
            if (file != null)
                fileName = file.getName();
            else
                fileName = "Untitled.md";
        }
        if (!filePath.endsWith("/"))
            filePath += "/";
        final boolean result;
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    PrintWriter writer = new PrintWriter(strings[0], "UTF-8");
                    writer.write(markdownLiveData.getValue());
                } catch (IOException e) {
                    Log.e(TAG, "Error saving file: ", e);
                }
                return null;
            }
        }.execute(filePath + fileName);
        return true;
    }

    public void requestSave(String s) {
        // Do something to save the file?
    }

    public String getMarkdown() {
        return markdownLiveData.getValue();
    } */
}
