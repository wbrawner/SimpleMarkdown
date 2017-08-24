# Introduction

***

Markdown is a markup language that allows you to quickly and easily write for the web! Many blogging platforms allow you to use markdown in your posts, WordPress for example. This document has also been written in Markdown :)

Using Markdown is quite simple. There are just a few syntax rules you'll need to remember. Below I have written a few to get you started, followed by their appearance in this app.

# Headings/Titles

***

`# Biggest`

# Biggest

`## Bigger`

## Bigger

`### Big`

### Big

`#### Small`

#### Small

`##### Smaller`

##### Smaller

`###### Smallest`

###### Smallest

```
Alt Biggest
=========
```

Alt Biggest
=========

```
Alt Bigger
---------------
```

Alt Bigger
---------------

# Inline Elements

***

`*italics*`

*italics*

`**BOLD**`

**BOLD**

`~~strikethrough~~`

~~strikethrough~~

```
`monospace`
```

`monospace`

```
[Here's a link](https://github.com/wbrawner/simplemarkdown)
```

[Here's a link](https://github.com/wbrawner/simplemarkdown)

# Block-level Elements

***

### Tables

Note that with tables, the placement of the colon character (`:`) determines the alignment of the text inside.

```
Left Content|Center Content|Right Content
:--------|:--------:|--------:
data 1|data 2|data 3
data 1|data 2|data 3
data 1|data 2|data 3
```

Left Content|Center Content|Right Content
:--------|:--------:|--------:
data 1|data 2|data 3
data 1|data 2|data 3
data 1|data 2|data 3

### Images

Note that with images, the text in the square brackets corresponds to the alt text of the image.

```
![My Logo](https://wbrawner.com/wp-content/uploads/cubiq-add-to-home/application-icon-305-196x196.png)
```

![My Logo](https://wbrawner.com/wp-content/uploads/cubiq-add-to-home/application-icon-305-196x196.png)

### Code 

In addition to the monospace inline element, code blocks can be created by indenting each line a minimum of 4 spaces:

    public static void main(String[] args) {
        System.out.println("Hello, world!");
    }

Or by wrapping the code in three backticks (\`\`\`):

```
function helloWorld() {
    console.log("Hello, world!")
}
```

### Line breaks

`***`

***

### Blockquotes

```
> What if I want to quote someone?

> asking for a friend
```

> What if I want to quote someone?

> asking for a friend

### Lists

Unordered lists can be created by either using asterisks (*) or hyphens (-). Indenting them by two spaces will also indent the bullets.

* Get rich
  * Buy an island
- Learn Android development
  - Contribute to open source software 

Numbered lists work similarly, though the actual number you use is irrelevant.

1. Do work
  0. get paid
  3. save money 
0. the actual number isn't important
  234234. it just needs to be a number 

# Final Notes

Don't forget that you can, of course, write regular HTML here and it will get parsed as normal. You can view this file here: [link](https://github.com/wbrawner/SimpleMarkdown/blob/master/app/src/main/assets/Cheatsheet.md)

If you need any assistance with the app or you run into a bug, please [open an issue](https://github.com/wbrawner/SimpleMarkdown/). You can also [visit me on my personal website](https://wbrawner.com), or [send me an email](billybrawner@gmail.com).