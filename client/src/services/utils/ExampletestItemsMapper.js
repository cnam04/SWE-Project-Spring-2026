/**
 * UTIL LAYER (inside services):
 * Small, pure helper functions used by service logic.
 *
 * Why this layer exists:
 * - Keeps data transformation reusable and easy to unit test.
 * - Prevents mapping details from being duplicated in handlers/pages.
 */
export function mapTestItemsForList(items) {
  if (!Array.isArray(items)) {
    return []
  }

  return items.map((item) => ({
    id: item.id,
    // Provide a safe default so UI remains stable even with partial data.
    name: item.name ?? 'Unnamed item',
  }))
}
