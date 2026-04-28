import { useCallback, useEffect, useState } from 'react'
import { loadTestItems } from '../../services/ExampletestItemsService'

/**
 * HANDLER LAYER:
 * This hook coordinates page behavior and state transitions.
 *
 * Why this layer exists:
 * - Keeps orchestration logic out of JSX-heavy page files.
 * - Converts UI events (refresh, initial load) into service calls.
 * - Gives pages a small, clean interface for state + actions.
 */
export function useTestItemsPageHandler() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [isRefreshing, setIsRefreshing] = useState(false)

  const fetchItems = useCallback(async (showRefreshingState) => {
    if (showRefreshingState) {
      setIsRefreshing(true)
    }

    try {
      const nextItems = await loadTestItems()
      setItems(nextItems)
      setError('')
    } catch (err) {
      setError(err.message || 'Unable to load test items')
    } finally {
      setLoading(false)
      setIsRefreshing(false)
    }
  }, [])

  useEffect(() => {
    fetchItems(false)
  }, [fetchItems])

  const handleRefresh = useCallback(() => {
    fetchItems(true)
  }, [fetchItems])

  return {
    items,
    loading,
    error,
    isRefreshing,
    handleRefresh,
  }
}
