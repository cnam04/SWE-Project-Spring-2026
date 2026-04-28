import { getTestItems } from '../Exampleapi'
import { mapTestItemsForList } from './utils/ExampletestItemsMapper'

/**
 * SERVICE LAYER:
 * This file contains feature-level frontend business logic.
 *
 * Why this layer exists:
 * - Keeps backend response shaping and app rules out of pages/components.
 * - Is the only layer that knows both feature needs and API helpers.
 * - Returns data already shaped for UI use.
 */
export async function loadTestItems() {
  const rawItems = await getTestItems()
  return mapTestItemsForList(rawItems)
}
