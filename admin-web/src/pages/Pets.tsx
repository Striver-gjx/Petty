import { useEffect, useState } from 'react';
import { PawPrint } from 'lucide-react';
import { petApi } from '../api';

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

const speciesMap: Record<string, string> = {
  CAT: '猫',
  DOG: '狗',
  BIRD: '鸟',
  FISH: '鱼',
  REPTILE: '爬行动物',
  SMALL_ANIMAL: '小动物',
  OTHER: '其他',
};

export default function PetsPage() {
  const [pets, setPets] = useState<Pet[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    petApi.list()
      .then((res) => setPets((res as { data: Pet[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-gray-400">加载中...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <PawPrint className="w-6 h-6" /> 宠物档案
        </h2>
        <span className="text-sm text-gray-400">{pets.length} 只宠物</span>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {pets.map((p) => (
          <div key={p.id} className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center">
                <PawPrint className="w-5 h-5 text-orange-500" />
              </div>
              <div>
                <h3 className="font-bold text-gray-800">{p.name}</h3>
                <p className="text-xs text-gray-400">{speciesMap[p.species] || p.species} · {p.breed || '未知品种'}</p>
              </div>
            </div>
            <div className="space-y-2 text-sm text-gray-600">
              {p.weight && <p>体重: {p.weight}kg</p>}
              {p.personality && <p>性格: {p.personality}</p>}
              {p.dietInfo && <p className="text-xs text-gray-400 line-clamp-2">饮食: {p.dietInfo}</p>}
            </div>
          </div>
        ))}
        {pets.length === 0 && (
          <div className="col-span-full text-center py-12 text-gray-400">暂无宠物数据</div>
        )}
      </div>
    </div>
  );
}
