/**
 * COMPONENT LAYER:
 * Reusable presentational UI that receives props from the page.
 *
 * Why this layer exists:
 * - Focuses on rendering and light interaction only.
 * - Avoids backend/request knowledge.
 * - Makes UI easier to reuse across screens.
 */
function TestItemsList({ items, loading, error }) {
  if (loading) {
    return <p>Loading...</p>
  }

  if (error) {
    return <p className="error-text">Error: {error}</p>
  }

  if (!items.length) {
    return <p>No test items found.</p>
  }

  return (
    <ul className="item-list">
      {items.map((item) => (
        <li key={item.id}>
          {item.id}: {item.name}
        </li>
      ))}
    </ul>
  )
}

export default TestItemsList
