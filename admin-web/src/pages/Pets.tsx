import { useEffect, useState } from 'react';
import { PawPrint, Search, Plus, Pencil, Trash2 } from 'lucide-react';
import { petApi } from '../api';
import Modal from '../components/Modal';

interface Pet {
  id: number;
  ownerId: number;
  name: string;
  species: string;
  breed: string;
  gender: string;
  weight: number;
  personality: string;
  dietInfo: string;
}

interface PetForm {
  ownerId: number;
  name: string;
  species: string;
  breed: string;
  gender: string;
  weight: number;
  personality: string;
  dietInfo: string;
}

const emptyForm: PetForm = {
  ownerId: 0, name: '', species: 'CAT', breed: '', gender: 'UNKNOWN', weight: 0, personality: '', dietInfo: '',
};

const speciesMap: Record<string, string> = {
  CAT: '猫', DOG: '狗', BIRD: '鸟', FISH: '鱼',
  REPTILE: '爬行动物', SMALL_ANIMAL: '小动物', OTHER: '其他',
};

const genderMap: Record<string, string> = {
  MALE: '公', FEMALE: '母', UNKNOWN: '未知',
};

const INPUT = 'w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500';

export default function PetsPage() {
  const [pets, setPets] = useState<Pet[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<PetForm>(emptyForm);
  const [saving, setSaving] = useState(false);

  const [deleteId, setDeleteId] = useState<number | null>(null);

  useEffect(() => {
    petApi.list()
      .then((res) => setPets((res as { data: Pet[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function refreshList() {
    petApi.list()
      .then((res) => setPets((res as { data: Pet[] }).data))
      .catch(() => {});
  }

  function openCreate() {
    setForm(emptyForm);
    setEditingId(null);
    setFormOpen(true);
  }

  function openEdit(p: Pet) {
    setForm({
      ownerId: p.ownerId,
      name: p.name,
      species: p.species,
      breed: p.breed,
      gender: p.gender,
      weight: p.weight,
      personality: p.personality,
      dietInfo: p.dietInfo,
    });
    setEditingId(p.id);
    setFormOpen(true);
  }

  async function handleSubmit() {
    setSaving(true);
    try {
      const payload: Record<string, unknown> = { ...form };
      if (editingId !== null) {
        await petApi.update(editingId, payload);
      } else {
        await petApi.create(payload);
      }
      setFormOpen(false);
      refreshList();
    } catch (err) {
      alert('操作失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
    setSaving(false);
  }

  async function handleDelete() {
    if (deleteId === null) return;
    try {
      await petApi.delete(deleteId);
      setDeleteId(null);
      refreshList();
    } catch (err) {
      alert('删除失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
  }

  const filtered = pets.filter((p) => {
    if (!search) return true;
    const q = search.toLowerCase();
    return p.name.toLowerCase().includes(q)
      || (speciesMap[p.species] ?? '').includes(q)
      || (p.breed ?? '').toLowerCase().includes(q);
  });

  if (loading) return <div className="text-gray-400">加载中...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <PawPrint className="w-6 h-6" /> 宠物档案
        </h2>
        <button
          onClick={openCreate}
          className="flex items-center gap-1.5 px-4 py-2 bg-orange-500 hover:bg-orange-600 text-white rounded-lg text-sm font-medium transition-colors"
        >
          <Plus className="w-4 h-4" /> 添加
        </button>
      </div>

      <div className="relative max-w-sm mb-4">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          placeholder="搜索名字、种类或品种..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filtered.map((p) => (
          <div key={p.id} className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center">
                  <PawPrint className="w-5 h-5 text-orange-500" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-800">{p.name}</h3>
                  <p className="text-xs text-gray-400">
                    {speciesMap[p.species] ?? p.species} · {p.breed || '未知品种'}
                    {p.gender && ` · ${genderMap[p.gender] ?? p.gender}`}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-1">
                <button onClick={() => openEdit(p)} className="p-1.5 text-gray-400 hover:text-orange-500 transition-colors" title="编辑">
                  <Pencil className="w-4 h-4" />
                </button>
                <button onClick={() => setDeleteId(p.id)} className="p-1.5 text-gray-400 hover:text-red-500 transition-colors" title="删除">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
            <div className="space-y-2 text-sm text-gray-600">
              <p className="text-xs text-gray-400">主人 ID: {p.ownerId}</p>
              {p.weight > 0 && <p>体重: {p.weight}kg</p>}
              {p.personality && <p>性格: {p.personality}</p>}
              {p.dietInfo && <p className="text-xs text-gray-400 line-clamp-2">饮食: {p.dietInfo}</p>}
            </div>
          </div>
        ))}
        {filtered.length === 0 && (
          <div className="col-span-full text-center py-12 text-gray-400">暂无宠物数据</div>
        )}
      </div>

      {/* Add/Edit Form Modal */}
      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editingId !== null ? '编辑宠物' : '添加宠物'}>
        <div className="space-y-4">
          {editingId === null && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">主人 ID</label>
              <input
                type="number" min={1}
                value={form.ownerId || ''}
                onChange={(e) => setForm({ ...form, ownerId: Number(e.target.value) || 0 })}
                className={INPUT}
                placeholder="请输入主人 ID"
              />
            </div>
          )}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">名字</label>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className={INPUT} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">种类</label>
              <select value={form.species} onChange={(e) => setForm({ ...form, species: e.target.value })} className={INPUT}>
                {Object.entries(speciesMap).map(([k, v]) => (
                  <option key={k} value={k}>{v}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">性别</label>
              <select value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })} className={INPUT}>
                {Object.entries(genderMap).map(([k, v]) => (
                  <option key={k} value={k}>{v}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">品种</label>
              <input value={form.breed} onChange={(e) => setForm({ ...form, breed: e.target.value })} className={INPUT} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">体重 (kg)</label>
              <input
                type="number" min={0} step={0.1}
                value={form.weight || ''}
                onChange={(e) => setForm({ ...form, weight: Number(e.target.value) || 0 })}
                className={INPUT}
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">性格</label>
            <input value={form.personality} onChange={(e) => setForm({ ...form, personality: e.target.value })} className={INPUT} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">饮食信息</label>
            <textarea value={form.dietInfo} onChange={(e) => setForm({ ...form, dietInfo: e.target.value })} rows={2} className={INPUT} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setFormOpen(false)} className="px-4 py-2 text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
              取消
            </button>
            <button onClick={handleSubmit} disabled={saving} className="px-4 py-2 text-sm text-white bg-orange-500 hover:bg-orange-600 disabled:opacity-50 rounded-lg font-medium transition-colors">
              {saving ? '保存中...' : '保存'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal open={deleteId !== null} onClose={() => setDeleteId(null)} title="确认删除">
        <div>
          <p className="text-gray-600 mb-6">确定要删除该宠物档案吗？此操作不可撤销。</p>
          <div className="flex justify-end gap-3">
            <button onClick={() => setDeleteId(null)} className="px-4 py-2 text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
              取消
            </button>
            <button onClick={handleDelete} className="px-4 py-2 text-sm text-white bg-red-500 hover:bg-red-600 rounded-lg font-medium transition-colors">
              确认删除
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
