import React from 'react';

export default function AdminPage() {
  return (
    <section className="app-page">
      <div className="container is-app">
        <header className="app-page-header">
          <h1 className="title is-4 app-page-title">Admin</h1>
          <p className="subtitle is-6 app-page-subtitle">
            Administrative actions and configuration tools will live on this screen.
          </p>
        </header>

        <section className="box app-surface">
          <p className="has-text-grey">No admin controls are wired yet.</p>
        </section>
      </div>
    </section>
  );
}
