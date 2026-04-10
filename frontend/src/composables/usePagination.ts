import { computed, ref } from 'vue'

export function usePagination(initialPage = 1, initialPageSize = 10) {
  const page = ref(initialPage)
  const pageSize = ref(initialPageSize)
  const hasNext = ref(false)

  const canPrev = computed(() => page.value > 1)

  function setPage(next: number) {
    page.value = Math.max(1, next)
  }

  function nextPage() {
    if (hasNext.value) {
      page.value += 1
    }
  }

  function prevPage() {
    if (canPrev.value) {
      page.value -= 1
    }
  }

  function syncHasNext(itemCount: number) {
    hasNext.value = itemCount >= pageSize.value
  }

  return {
    page,
    pageSize,
    hasNext,
    canPrev,
    setPage,
    nextPage,
    prevPage,
    syncHasNext,
  }
}
