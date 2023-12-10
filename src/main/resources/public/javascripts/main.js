function setupFootnotesAnimationEvent() {
    const pageContents = document.querySelector(".page-contents");
    if (pageContents) {
        pageContents
            .querySelectorAll(".footnote-ref")
            .forEach(note => {
                note.addEventListener("click", () => {
                    const noteId = note.href.split("#")[1]
                    const noteElement = document.getElementById(noteId);
                    if (noteElement) {
                        noteElement.classList.add("blink-yellow");
                        // Remove the class after sometime so that the same footnote
                        // will blink when user clicks it a second time.
                        setTimeout(() => {
                            noteElement.classList.remove("blink-yellow");
                        }, 3000)
                    }
                })
            });
    }
}

window.addEventListener("load", setupFootnotesAnimationEvent);
document.documentElement.addEventListener("htmx:load", setupFootnotesAnimationEvent);