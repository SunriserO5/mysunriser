import { ref } from 'vue'
import { fetchPost } from '../api'
import type { PostDetail } from '../types'

export function usePost() {
  const post = ref<PostDetail | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function loadPost(slug: string) {
    loading.value = true
    error.value = null

    try {
      post.value = await fetchPost(slug)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载文章失败'
      post.value = null
    } finally {
      loading.value = false
    }
  }

  return {
    post,
    loading,
    error,
    loadPost,
  }
}
