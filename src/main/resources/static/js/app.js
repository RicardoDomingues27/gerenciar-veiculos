const API = '/veiculos';

document.addEventListener('DOMContentLoaded', () => {
    if (localStorage.getItem('token')) {
        loginSuccess();
    }
});

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    };
}

/* TOAST */
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.innerText = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 4000);
}

/* LOGIN */
function login() {
    fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            username: username.value,
            password: password.value
        })
    })
    .then(res => {
        if (!res.ok) {
            showToast('Usuário ou senha inválidos', 'error');
            throw new Error();
        }
        return res.json();
    })
    .then(data => {
        localStorage.setItem('token', data.token);
        showToast('Login realizado com sucesso');
        loginSuccess();
    });
}

function loginSuccess() {
    document.getElementById('login').classList.add('hidden');
    document.getElementById('app').classList.remove('hidden');
    loadVeiculos();
}

/* LOGOUT */
function logout() {
    localStorage.removeItem('token');
    showToast('Logout realizado com sucesso');
    setTimeout(() => location.reload(), 800);
}

/* LISTAR */
function loadVeiculos() {
    fetch(API, { headers: authHeaders() })
        .then(res => res.json())
        .then(data => {
            const ul = document.getElementById('veiculos');
            const empty = document.getElementById('emptyMessage');
            ul.innerHTML = '';

            const veiculos = data.content || [];

            if (veiculos.length === 0) {
                empty.style.display = 'block';
                return;
            }

            empty.style.display = 'none';

            veiculos.forEach(v => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <span>
                        <strong>${v.marca} ${v.modelo}</strong><br>
                        Ano: ${v.ano} | Cor: ${v.cor}<br>
                        Placa: ${v.placa} | R$ ${v.valor}
                    </span>
                    <div class="actions">
                        <button class="edit" onclick='editVeiculo(${JSON.stringify(v)})'>Editar</button>
                        <button class="delete" onclick='deleteVeiculo(${v.id})'>Excluir</button>
                    </div>
                `;
                ul.appendChild(li);
            });
        })
        .catch(() => {
            showToast('Erro ao carregar veículos', 'error');
        });
}

/* SALVAR */
function saveVeiculo() {
    const id = veiculoId.value;

    const veiculo = {
        marca: marca.value,
        modelo: modelo.value,
        ano: ano.value,
        cor: cor.value,
        placa: placa.value,
        precoDolar: precoDolar.value
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${API}/${id}` : API;

    fetch(url, {
        method,
        headers: authHeaders(),
        body: JSON.stringify(veiculo)
    })
    .then(res => {
        if (!res.ok) {
            return res.text().then(msg => {
                if (msg.toLowerCase().includes('placa')) {
                    showToast('Placa já cadastrada', 'error');
                } else {
                    showToast('Veículo já cadastrado', 'error');
                }
                throw new Error();
            });
        }
        showToast('Veículo salvo com sucesso');
        clearForm();
        loadVeiculos();
    });
}

/* EDITAR */
function editVeiculo(v) {
    veiculoId.value = v.id;
    marca.value = v.marca;
    modelo.value = v.modelo;
    ano.value = v.ano;
    cor.value = v.cor;
    placa.value = v.placa;
    valor.value = v.valor;
}

/* EXCLUIR */
function deleteVeiculo(id) {
    if (!confirm('Deseja excluir este veículo?')) return;

    fetch(`${API}/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
    })
    .then(res => {
        if (!res.ok) {
            showToast('Erro ao excluir veículo', 'error');
            throw new Error();
        }
        showToast('Veículo excluído com sucesso');
        loadVeiculos();
    });
}

function clearForm() {
    veiculoId.value = '';
    marca.value = '';
    modelo.value = '';
    ano.value = '';
    cor.value = '';
    placa.value = '';
    valor.value = '';
}
