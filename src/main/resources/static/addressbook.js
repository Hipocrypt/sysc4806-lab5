'use strict';

async function postJson(path, body) {
    const response = await fetch(path, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    });
    if (!response.ok) {
        throw new Error(`Could not save (${response.status}). Check the fields and try again.`);
    }
    return response.json();
}

function onSubmit(id, action) {
    const form = document.getElementById(id);
    if (!form) return;
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const button = form.querySelector('button');
        const status = document.getElementById('status');
        button.disabled = true;
        status.textContent = 'Saving…';
        try {
            await action(form);
        } catch (error) {
            status.textContent = error.message;
        } finally {
            button.disabled = false;
        }
    });
}

onSubmit('create-book', async () => {
    const book = await postJson('/api/addressbooks', {});
    window.location.assign(`/addressbooks/${book.id}`);
});

onSubmit('open-book', async form => {
    const id = new FormData(form).get('bookId');
    const response = await fetch(`/api/addressbooks/${encodeURIComponent(id)}`);
    if (!response.ok) throw new Error('That book could not be found. Check the ID or create a new book.');
    window.location.assign(`/addressbooks/${encodeURIComponent(id)}`);
});

// Retain the created buddy if attaching fails, so retrying does not create another one.
let pendingBuddy;
onSubmit('add-buddy', async form => {
    if (!pendingBuddy) {
        const data = new FormData(form);
        pendingBuddy = await postJson('/api/buddies', {
            firstName: data.get('firstName').trim(),
            lastName: data.get('lastName').trim(),
            address: Number(data.get('address')),
            phone: Number(data.get('phone')),
            email: data.get('email').trim() || null
        });
    }
    await postJson(`/api/addressbooks/${form.dataset.bookId}/buddies`, {buddyId: pendingBuddy.id});
    window.location.reload();
});
