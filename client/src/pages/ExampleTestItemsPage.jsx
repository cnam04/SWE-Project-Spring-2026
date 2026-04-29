import { useTestItemsPageHandler } from './handlers/ExampleuseTestItemsPageHandler'
import TestItemsList from '../components/ExampleTestItemsList'

/**
 * PAGE LAYER:
 * This file represents a route-level screen in the app.
 *
 * Why this layer exists:
 * - It composes UI sections/components for one screen.
 * - It triggers high-level user actions through a handler.
 * - It does not contain HTTP details or backend contract logic.
 */
function TestItemsPage() {
  const {
    items,
    loading,
    error,
    isRefreshing,
    handleRefresh,
  } = useTestItemsPageHandler()

  return (
    <section className="app-page">
      <div className="container is-app">
        <header className="app-page-header">
          <h1 className="title is-4 app-page-title">Course Prereq Visualizer</h1>
          <p className="subtitle is-6 app-page-subtitle">
            Layered frontend example: Page -&gt; Handler -&gt; Service -&gt; util -&gt; api.js
          </p>
        </header>

        <section className="box app-surface mb-4">
          <h2 className="title is-5 mb-0">DB Test Connection</h2>
        </section>

        <section className="box app-surface">
          <div className="is-flex is-align-items-center is-justify-content-space-between is-gap-3 mb-3 is-flex-wrap-wrap">
            <h2 className="title is-5 mb-0">Backend test items</h2>
            <button className="button is-link is-light" type="button" onClick={handleRefresh} disabled={isRefreshing}>
              {isRefreshing ? 'Refreshing...' : 'Refresh'}
            </button>
          </div>

          <TestItemsList items={items} loading={loading} error={error} />
        </section>
      </div>
    </section>
  )
}

export default TestItemsPage
