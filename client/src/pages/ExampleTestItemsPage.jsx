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
    <main className="page-shell">
      <h1>Course Prereq Visualizer</h1>
      <p>Layered frontend example: Page -&gt; Handler -&gt; Service -&gt; util -&gt; api.js</p>
      <section className="card-shell">
        <div className="card-header">
          <h1>DB Test Connection</h1>
        </div>
      </section>
      <section className="card-shell">
        <div className="card-header">
          <h2>Backend test items</h2>
          <button type="button" onClick={handleRefresh} disabled={isRefreshing}>
            {isRefreshing ? 'Refreshing...' : 'Refresh'}
          </button>
        </div>

        <TestItemsList items={items} loading={loading} error={error} />
      </section>
    </main>
  )
}

export default TestItemsPage
