import {Collapse} from "bootstrap";

function setupFootnotesAnimationEvent() {
    const pageContents = document.querySelector(".page-contents");
    if (pageContents) {
        pageContents
            .querySelectorAll(".footnote-ref")
            .forEach(note => {
                note.addEventListener("click", () => {
                    const noteId = note.href.split("#")[1];
                    const noteElement = document.getElementById(noteId);
                    if (noteElement) {
                        noteElement.classList.add("blink-yellow");
                        // Remove the class after sometime so that the same footnote
                        // will blink when user clicks it a second time.
                        setTimeout(() => noteElement.classList.remove("blink-yellow"), 3000);
                    }
                });
            });
    }
}

function delayHidingHtmxProgressBarWhenTooFast() {
    const progressBar = document.getElementById("request-progress-bar");
    if (progressBar) {
        progressBar.classList.add("htmx-request");

        // If the request completes too fast, this avoids the progress bar
        // to just flicking.
        setTimeout(() => progressBar.classList.remove("htmx-request"), 300);
    }
}

function setupCollapsableComponents() {
    const collapseElementList = document.querySelectorAll(".collapse");
    [...collapseElementList].forEach(collapseEl =>
        collapseEl.addEventListener("click", () => new Collapse(collapseEl))
    );
}

window.addEventListener("load", setupCollapsableComponents);
window.addEventListener("load", setupFootnotesAnimationEvent);

// https://htmx.org/events/#htmx:load
document.documentElement.addEventListener("htmx:load", setupFootnotesAnimationEvent);

// https://htmx.org/events/#htmx:afterRequest
document.documentElement.addEventListener("htmx:afterRequest", delayHidingHtmxProgressBarWhenTooFast);